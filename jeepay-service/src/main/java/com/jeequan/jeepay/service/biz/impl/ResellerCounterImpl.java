package com.jeequan.jeepay.service.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.service.impl.SysUserService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.service.biz.ResellerOrderCounter;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderCountVo;

/**
 * @author axl rose
 * @date 2021/10/4
 */
@Service
public class ResellerCounterImpl implements ResellerOrderCounter {

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Autowired
  private SysUserService sysUserService;

  @Override
  public IPage<ResellerOrderCountVo> getResellerCounterPage(ResellerOrder resellerOrder,
      String startDay, String endDay, IPage iPage) {
    LambdaQueryWrapper<ResellerOrder> resellerOrderQueryWrapper = ResellerOrder.gw();
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getProductType, resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getResellerNo, resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getChargeAccount())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getChargeAccount,
          resellerOrder.getChargeAccount());
    }
    if (StringUtils.isNotBlank(resellerOrder.getQueryFlag())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getQueryFlag, resellerOrder.getQueryFlag());
    }
    if (StringUtils.isNotBlank(startDay)) {
      resellerOrderQueryWrapper.ge(ResellerOrder::getGmtCreate, startDay);
    }
    if (StringUtils.isNotBlank(endDay)) {
      resellerOrderQueryWrapper.le(ResellerOrder::getGmtCreate, endDay);
    }
    resellerOrderQueryWrapper.select(ResellerOrder::getChargeAccount, ResellerOrder::getProductType,
        ResellerOrder::getResellerNo, ResellerOrder::getQueryFlag);
    resellerOrderQueryWrapper.groupBy(ResellerOrder::getChargeAccount,
        ResellerOrder::getProductType,
        ResellerOrder::getResellerNo, ResellerOrder::getQueryFlag);
    IPage<ResellerOrder> iPageResellerOrder = resellerOrderService.page(iPage,
        resellerOrderQueryWrapper);
    IPage<ResellerOrderCountVo> iPageResult = new Page<>(iPageResellerOrder.getCurrent(),
        iPageResellerOrder.getSize());
    if (iPageResellerOrder.getSize() <= 0 || iPageResellerOrder.getRecords() == null &&
        iPageResellerOrder.getRecords().size() < 0) {
      return iPageResult;
    }

    iPageResult.setPages(iPageResellerOrder.getPages());
    iPageResult.setTotal(iPageResellerOrder.getTotal());
    List<ResellerOrderCountVo> resellerOrderCountVoList = new ArrayList<>();
    for (ResellerOrder tempResellerOrder : iPageResellerOrder.getRecords()) {
      ResellerOrderCountVo resellerOrderCountVo = new ResellerOrderCountVo();
      resellerOrderCountVo.setChargeAccount(tempResellerOrder.getChargeAccount());
      resellerOrderCountVo.setResellerNo(tempResellerOrder.getResellerNo());
      resellerOrderCountVo.setProductType(tempResellerOrder.getProductType());
      resellerOrderCountVo.setQueryFlag(tempResellerOrder.getQueryFlag());
      resellerOrderCountVoList.add(resellerOrderCountVo);
    }
    iPageResult.setRecords(resellerOrderCountVoList);
    setResellerName(iPageResult.getRecords(), resellerOrder, startDay, endDay);
    return iPageResult;
  }

  private void setResellerName(List<ResellerOrderCountVo> iPageResult, ResellerOrder resellerOrder,
      String startDate, String endDate) {
    if (iPageResult.size() > 0) {
      List<String> resellerNos = iPageResult.stream()
          .map(resellerOrderCountVo -> resellerOrderCountVo.getResellerNo()).collect(
              Collectors.toList());
      LambdaQueryWrapper<SysUser> sysUserLambdaQueryWrapper = SysUser.gw();
      sysUserLambdaQueryWrapper.in(SysUser::getUserNo, resellerNos);
      List<SysUser> sysUsers = sysUserService.list(sysUserLambdaQueryWrapper);
      Map<String, List<SysUser>> sysUserMap = sysUsers.stream()
          .collect(Collectors.groupingBy(SysUser::getUserNo));
      for (ResellerOrderCountVo resellerOrderCountVo : iPageResult) {
        SysUser sysUser = sysUserMap.get(resellerOrderCountVo.getResellerNo()).get(0);
        resellerOrderCountVo.setResellerName(sysUser.getRealname());
        long totalAmount = queryOrderAllAmount(resellerOrder,
            resellerOrderCountVo.getChargeAccount(), null, resellerOrderCountVo.getProductType(),
            startDate, endDate,resellerOrderCountVo.getQueryFlag());
        long totalFinishAmount = queryOrderAllAmount(resellerOrder,
            resellerOrderCountVo.getChargeAccount(),
            ResellerOrderStatusEnum.FINISH.getCode(), resellerOrderCountVo.getProductType(),
            startDate, endDate,resellerOrderCountVo.getQueryFlag());
        resellerOrderCountVo.setOrderAllAmount(totalAmount);
        resellerOrderCountVo.setFinishAllAmount(totalFinishAmount);
        if (resellerOrderCountVo.getFinishAllAmount() >= resellerOrderCountVo.getOrderAllAmount()) {
          resellerOrderCountVo.setOrderStatus("订单完成");
        } else {
          resellerOrderCountVo.setOrderStatus("订单未完成");
        }
        resellerOrderCountVo.setProductType(
            ProductTypeEnum.getType(resellerOrderCountVo.getProductType()).getMsg());
      }
    }
  }

  private Long queryOrderAllAmount(ResellerOrder resellerOrder, String chargeAccount, String status,
      String productType, String startDate, String endDate,String queryFlag) {
    QueryWrapper<ResellerOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("ifnull(sum(amount),0) as total");
    queryWrapper.eq("product_type", productType);
    queryWrapper.eq("charge_account", chargeAccount);
    if (StringUtils.isNotBlank(queryFlag)) {
      queryWrapper.eq("query_flag", queryFlag);
    }
    queryWrapper.eq("reseller_no", resellerOrder.getResellerNo());
    if (StringUtils.isNotBlank(status)) {
      queryWrapper.le("order_status", status);
    }
    if (StringUtils.isNotBlank(startDate)) {
      queryWrapper.ge("gmt_create", startDate);
    }
    if (StringUtils.isNotBlank(endDate)) {
      queryWrapper.le("gmt_create", endDate);
    }
    Map<String, Object> resultMap = resellerOrderService.getMap(queryWrapper);
    if (resultMap.size() > 0 && resultMap.get("total") != null) {
      Long total = ((BigDecimal) resultMap.get("total")).longValue();
      return total;
    }
    return 0L;
  }

  @Override
  public List<ResellerOrderCountVo> getResellerCounterList(ResellerOrder resellerOrder,
      String startDay, String endDay) {
    LambdaQueryWrapper<ResellerOrder> resellerOrderQueryWrapper = ResellerOrder.gw();
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getProductType, resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getResellerNo, resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getChargeAccount())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getChargeAccount,
          resellerOrder.getChargeAccount());
    }
    if (StringUtils.isNotBlank(resellerOrder.getQueryFlag())) {
      resellerOrderQueryWrapper.eq(ResellerOrder::getQueryFlag, resellerOrder.getQueryFlag());
    }
    if (StringUtils.isNotBlank(startDay)) {
      resellerOrderQueryWrapper.ge(ResellerOrder::getGmtCreate, startDay);
    }
    if (StringUtils.isNotBlank(endDay)) {
      resellerOrderQueryWrapper.le(ResellerOrder::getGmtCreate, endDay);
    }
    resellerOrderQueryWrapper.select(ResellerOrder::getChargeAccount, ResellerOrder::getProductType,
        ResellerOrder::getResellerNo);
    resellerOrderQueryWrapper.groupBy(ResellerOrder::getChargeAccount,
        ResellerOrder::getProductType,
        ResellerOrder::getResellerNo);
    List<ResellerOrder> iPageResellerOrder = resellerOrderService.list(resellerOrderQueryWrapper);

    List<ResellerOrderCountVo> resellerOrderCountVoList = new ArrayList<>();
    for (ResellerOrder tempResellerOrder : iPageResellerOrder) {
      ResellerOrderCountVo resellerOrderCountVo = new ResellerOrderCountVo();
      resellerOrderCountVo.setChargeAccount(tempResellerOrder.getChargeAccount());
      resellerOrderCountVo.setResellerNo(tempResellerOrder.getResellerNo());
      resellerOrderCountVo.setProductType(tempResellerOrder.getProductType());
      resellerOrderCountVoList.add(resellerOrderCountVo);
    }
    setResellerName(resellerOrderCountVoList, resellerOrder, startDay, endDay);
    return resellerOrderCountVoList;
  }

}
