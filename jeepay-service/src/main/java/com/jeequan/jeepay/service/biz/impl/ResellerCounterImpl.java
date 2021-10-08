package com.jeequan.jeepay.service.biz.impl;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.core.entity.SysUser;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.service.impl.SysUserService;
import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.service.biz.ResellerOrderCounter;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderCountVo;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;
import com.jeequan.jeepay.service.biz.vo.ResellerSimpleCountVo;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderOverallView;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderFundOverallView;
import com.jeequan.jeepay.service.impl.ResellerPretenderProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

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

  @Autowired
  private ResellerPretenderProductService resellerPretenderProductService;

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
            startDate, endDate, resellerOrderCountVo.getQueryFlag());
        long totalFinishAmount = queryOrderAllAmount(resellerOrder,
            resellerOrderCountVo.getChargeAccount(),
            ResellerOrderStatusEnum.FINISH.getCode(), resellerOrderCountVo.getProductType(),
            startDate, endDate, resellerOrderCountVo.getQueryFlag());
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
      String productType, String startDate, String endDate, String queryFlag) {
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

  @Override
  public List<ResellerOrderOverallView> countOverallViewByAmount(ResellerOrder resellerOrder,
      String startDay,
      String endDay) {
    //query all count reseller oder count
    List<ResellerSimpleCountVo> allResellerCountVo = countResellerSimpleCountVoByAmount(null,
        startDay, endDay, resellerOrder);
    List<ResellerSimpleCountVo> allWaitResellerCountVo = countResellerSimpleCountVoByAmount(
        ResellerOrderStatusEnum.WAITING_MATCH.getCode(), startDay,
        endDay, resellerOrder);
    List<ResellerSimpleCountVo> allPayResellerCountVo = countResellerSimpleCountVoByAmount(
        ResellerOrderStatusEnum.PAYING.getCode(), startDay, endDay, resellerOrder);
    List<ResellerSimpleCountVo> allFinishResellerCountVo = countResellerSimpleCountVoByAmount(
        ResellerOrderStatusEnum.FINISH.getCode(), startDay, endDay, resellerOrder);
    List<ResellerSimpleCountVo> allSleepResellerCountVo = countResellerSimpleCountVoByAmount(
        ResellerOrderStatusEnum.SLEEP.getCode(), startDay, endDay, resellerOrder);
    Map<Long, List<ResellerSimpleCountVo>> allWaitResellerCountVoMap = allWaitResellerCountVo
        .stream()
        .collect(Collectors.groupingBy(ResellerSimpleCountVo::getAmount));
    Map<Long, List<ResellerSimpleCountVo>> allPayResellerCountVoMap = allPayResellerCountVo.stream()
        .collect(Collectors.groupingBy(ResellerSimpleCountVo::getAmount));
    Map<Long, List<ResellerSimpleCountVo>> allFinishResellerCountVoMap = allFinishResellerCountVo
        .stream()
        .collect(Collectors.groupingBy(ResellerSimpleCountVo::getAmount));
    Map<Long, List<ResellerSimpleCountVo>> allSleepResellerCountVoMap = allSleepResellerCountVo
        .stream()
        .collect(Collectors.groupingBy(ResellerSimpleCountVo::getAmount));
    List<ResellerOrderOverallView> resellerOrderOverallViewList = new ArrayList<>();
    for (ResellerSimpleCountVo resellerSimpleCountVo : allResellerCountVo) {
      if (resellerSimpleCountVo.getAmount() == null) {
        continue;
      }
      ResellerOrderOverallView resellerOrderOverallView = new ResellerOrderOverallView();
      resellerOrderOverallView.setFaceAmount(resellerSimpleCountVo.getAmount());
      resellerOrderOverallView.setAllCount(resellerSimpleCountVo.getAllCount());
      resellerOrderOverallView.setAllAmount(resellerSimpleCountVo.getAllAmount());
      if (allWaitResellerCountVoMap.containsKey(resellerSimpleCountVo.getAmount())) {
        Optional<ResellerSimpleCountVo> waitOpt = allWaitResellerCountVoMap.get(
            resellerSimpleCountVo.getAmount()).stream().findAny();
        if (waitOpt.isPresent()) {
          ResellerSimpleCountVo waitResellerSimpleCountVo = waitOpt.get();
          resellerOrderOverallView.setWaitAllAmount(waitResellerSimpleCountVo.getAllAmount());
          resellerOrderOverallView.setWaitCount(waitResellerSimpleCountVo.getAllCount());
        }
      }
      if (allPayResellerCountVoMap.containsKey(resellerSimpleCountVo.getAmount())) {
        Optional<ResellerSimpleCountVo> payIngOpt = allPayResellerCountVoMap.get(
            resellerSimpleCountVo.getAmount()).stream().findAny();
        if (payIngOpt.isPresent()) {
          ResellerSimpleCountVo payIngResellerSimpleCountVo = payIngOpt.get();
          resellerOrderOverallView.setPayingCount(payIngResellerSimpleCountVo.getAllCount());
          resellerOrderOverallView.setPayAllAmount(payIngResellerSimpleCountVo.getAllAmount());
        }
      }

      if (allFinishResellerCountVoMap.containsKey(resellerSimpleCountVo.getAmount())) {
        Optional<ResellerSimpleCountVo> finishOpt = allFinishResellerCountVoMap.get(
            resellerSimpleCountVo.getAmount()).stream().findAny();
        if (finishOpt.isPresent()) {
          ResellerSimpleCountVo finishIngResellerSimpleCountVo = finishOpt.get();
          resellerOrderOverallView.setFinishCount(finishIngResellerSimpleCountVo.getAllCount());
          resellerOrderOverallView.setFinishAllAmount(
              finishIngResellerSimpleCountVo.getAllAmount());
        }
      }

      if (allSleepResellerCountVoMap.containsKey(resellerSimpleCountVo.getAmount())) {
        Optional<ResellerSimpleCountVo> sleepOpt = allSleepResellerCountVoMap.get(
            resellerSimpleCountVo.getAmount()).stream().findAny();
        if (sleepOpt.isPresent()) {
          ResellerSimpleCountVo sleepResellerSimpleCountVo = sleepOpt.get();
          resellerOrderOverallView.setSleepCount(sleepResellerSimpleCountVo.getAllCount());
          resellerOrderOverallView.setSleepAllAmount(sleepResellerSimpleCountVo.getAllAmount());
        }
      }
      resellerOrderOverallView.setAllCount(resellerSimpleCountVo.getAllCount());
      resellerOrderOverallView.setAllAmount(resellerSimpleCountVo.getAllAmount());
      resellerOrderOverallView.setFaceAmount(resellerSimpleCountVo.getAmount());
      resellerOrderOverallViewList.add(resellerOrderOverallView);
    }
    return resellerOrderOverallViewList;
  }

  private List<ResellerSimpleCountVo> countResellerSimpleCountVoByAmount(String status,
      String startDay, String endDay, ResellerOrder resellerOrder) {
    QueryWrapper<ResellerOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.select(
        "amount as amount, ifnull(sum(amount),0) as allAmount,count(amount) as allCount");
    if (StringUtils.isNotBlank(status)) {
      queryWrapper.eq("order_status", status);
    }
    if (StringUtils.isNotBlank(startDay)) {
      queryWrapper.ge("gmt_create", startDay);
    }
    if (StringUtils.isNotBlank(endDay)) {
      queryWrapper.le("gmt_create", endDay);
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      queryWrapper.eq("reseller_no", resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      queryWrapper.eq("product_type", resellerOrder.getProductType());
    }

    queryWrapper.groupBy("amount");
    queryWrapper.orderByAsc("amount");
    List<ResellerSimpleCountVo> resellerSimpleCountVoList = new ArrayList<>();
    List<Map<String, Object>> resultListMap = resellerOrderService.listMaps(queryWrapper);
    if (resultListMap == null || resultListMap.size() <= 0) {
      return resellerSimpleCountVoList;
    }
    for (Map<String, Object> tempMap : resultListMap) {
      if (tempMap == null || tempMap.size() <= 0) {
        continue;
      }
      Object amountObj = tempMap.get("amount");
      Object allAmountObj = tempMap.get("allAmount");
      Object allCountObj = tempMap.get("allCount");
      ResellerSimpleCountVo resellerSimpleCountVo = new ResellerSimpleCountVo();
      if (amountObj != null) {
        resellerSimpleCountVo.setAmount(((Long) amountObj));
      }
      if (allAmountObj != null) {
        resellerSimpleCountVo.setAllAmount((((BigDecimal) allAmountObj).longValue()));
      }
      if (allCountObj != null) {
        resellerSimpleCountVo.setAllCount((Long) allCountObj);
      }
      resellerSimpleCountVoList.add(resellerSimpleCountVo);
    }
    return resellerSimpleCountVoList;
  }

  @Override
  public List<ResellerOrderFundOverallView> countReSellerOrderFundByReseller(String startDay,
      String endDay, ResellerOrder resellerOrder) {
    QueryWrapper<ResellerOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("reseller_no resellerNo");

    if (StringUtils.isNotBlank(startDay)) {
      queryWrapper.ge("gmt_create", startDay);
    }
    if (StringUtils.isNotBlank(endDay)) {
      queryWrapper.le("gmt_create", endDay);
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      queryWrapper.eq("reseller_no", resellerOrder.getResellerNo());
    }
    queryWrapper.groupBy("reseller_no");

    List<ResellerOrderFundOverallView> resellerOrderFundOverallViewList = new ArrayList<>();
    List<Map<String, Object>> maps = resellerOrderService.listMaps(queryWrapper);
    if (maps == null || maps.size() <= 0) {
      return resellerOrderFundOverallViewList;
    }
    for (Map<String, Object> tempMap : maps) {
      ResellerOrderFundOverallView resellerOrderFundOverallView = new ResellerOrderFundOverallView();
      String tempResellerNo = (String) tempMap.get("resellerNo");
      Long waitAmount = queryResellerAmount(ResellerOrderStatusEnum.WAITING_MATCH.getCode(),
          resellerOrder, startDay, endDay);
      resellerOrderFundOverallView.setResellerNo(resellerOrder.getResellerNo());
      SysUser sysUser = sysUserService.getOne(SysUser.gw().eq(SysUser::getUserNo, tempResellerNo));
      resellerOrderFundOverallView.setResellerName(sysUser.getRealname());
      resellerOrderFundOverallView.setAllWaitAmount(waitAmount);
      Long payAmount = queryResellerAmount(ResellerOrderStatusEnum.PAYING.getCode(), resellerOrder,
          startDay, endDay);
      resellerOrderFundOverallView.setAllPayAmount(payAmount);
      Long finishAmount = queryResellerAmount(ResellerOrderStatusEnum.FINISH.getCode(),
          resellerOrder, startDay, endDay);
      resellerOrderFundOverallView.setAllFinishAmount(finishAmount);
      ResellerPretenderProduct resellerPretenderProduct = resellerPretenderProductService.getOne(
          ResellerPretenderProduct.gw()
              .eq(ResellerPretenderProduct::getResellerNo, tempResellerNo));
      if (resellerPretenderProduct != null) {
        Long allReturnedAmount = resellerPretenderProduct.getFeeRate()
            .multiply(new BigDecimal(finishAmount)).longValue();
        resellerOrderFundOverallView.setAllReturnedAmount(allReturnedAmount);
      }
      Long sleepAmount = queryResellerAmount(ResellerOrderStatusEnum.SLEEP.getCode(), resellerOrder,
          startDay, endDay);
      resellerOrderFundOverallView.setAllSleepAmount(sleepAmount);
      Long allAmount = queryResellerAmount(null, resellerOrder, startDay, endDay);
      resellerOrderFundOverallView.setAllOrderAmount(allAmount);
      resellerOrderFundOverallViewList.add(resellerOrderFundOverallView);
    }
    return resellerOrderFundOverallViewList;
  }

  private Long queryResellerAmount(String status, ResellerOrder resellerOrder, String startDate,
      String endDate) {
    QueryWrapper<ResellerOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("ifnull(sum(amount),0)as amount");
    if (StringUtils.isNotBlank(startDate)) {
      queryWrapper.ge("gmt_create", startDate);
    }
    if (StringUtils.isNotBlank(endDate)) {
      queryWrapper.le("gmt_create", endDate);
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      queryWrapper.eq("reseller_no", resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(status)) {
      queryWrapper.eq("order_status", status);
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      queryWrapper.eq("product_type", resellerOrder.getProductType());
    }
    Map<String, Object> map = resellerOrderService.getMap(queryWrapper);
    if (map == null || map.size() <= 0) {
      return 0L;
    }
    Object amountObj = map.get("amount");
    if (amountObj == null) {
      return 0L;
    }
    Long amount = ((BigDecimal) map.get("amount")).longValue();
    return amount;
  }


  @Override
  public List<ResellerOrderFundOverallView> countReSellerOrderFundByQueryFlag(String startDay,
      String endDay, ResellerOrder resellerOrder) {
    List<ResellerOrderFundOverallView> resellerOrderFundOverallViewList = queryResellerOrderByQueryFlag(
        resellerOrder, startDay, endDay);
    for (ResellerOrderFundOverallView resellerOrderFundOverallView : resellerOrderFundOverallViewList) {
      if (StringUtils.isNotBlank(resellerOrderFundOverallView.getQueryFlag())) {
        resellerOrder.setQueryFlag(resellerOrderFundOverallView.getQueryFlag());
        Long allOrderAmount = sumResellerOrderByQueryFlag(resellerOrder, startDay, endDay);
        resellerOrder.setOrderStatus(ResellerOrderStatusEnum.WAITING_MATCH.getCode());
        Long allWaitAmount = sumResellerOrderByQueryFlag(resellerOrder, startDay, endDay);
        resellerOrder.setOrderStatus(ResellerOrderStatusEnum.FINISH.getCode());
        Long allFinishAmount = sumResellerOrderByQueryFlag(resellerOrder, startDay, endDay);
        ResellerPretenderProduct resellerPretenderProduct = resellerPretenderProductService.getOne(
            ResellerPretenderProduct.gw()
                .eq(ResellerPretenderProduct::getResellerNo,
                    resellerOrderFundOverallView.getResellerNo()));
        if (resellerPretenderProduct != null) {
          Long allReturnedAmount = resellerPretenderProduct.getFeeRate()
              .multiply(new BigDecimal(allFinishAmount)).longValue();
          resellerOrderFundOverallView.setAllReturnedAmount(allReturnedAmount);
        }
        resellerOrder.setOrderStatus(ResellerOrderStatusEnum.PAYING.getCode());
        Long allPayAmount = sumResellerOrderByQueryFlag(resellerOrder, startDay, endDay);
        resellerOrder.setOrderStatus(ResellerOrderStatusEnum.SLEEP.getCode());
        Long allSleepAmount = sumResellerOrderByQueryFlag(resellerOrder, startDay, endDay);
        resellerOrderFundOverallView.setAllOrderAmount(allOrderAmount);
        resellerOrderFundOverallView.setAllWaitAmount(allWaitAmount);
        resellerOrderFundOverallView.setAllFinishAmount(allFinishAmount);
        resellerOrderFundOverallView.setAllPayAmount(allPayAmount);
        resellerOrderFundOverallView.setAllSleepAmount(allSleepAmount);
      }
    }
    return resellerOrderFundOverallViewList;
  }

  private List<ResellerOrderFundOverallView> queryResellerOrderByQueryFlag(
      ResellerOrder resellerOrder, String startDate, String endDate) {
    QueryWrapper<ResellerOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("query_flag queryFlag,reseller_no resellerNo");
    if (StringUtils.isNotBlank(startDate)) {
      queryWrapper.ge("gmt_create", startDate);
    }
    if (StringUtils.isNotBlank(endDate)) {
      queryWrapper.le("gmt_create", endDate);
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      queryWrapper.eq("reseller_no", resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      queryWrapper.eq("product_type", resellerOrder.getProductType());
    }
    queryWrapper.groupBy("query_flag");

    List<ResellerOrderFundOverallView> resellerOrderFundOverallViewList = new ArrayList<>();
    List<Map<String, Object>> map = resellerOrderService.listMaps(queryWrapper);
    if (map == null || map.size() <= 0) {
      return resellerOrderFundOverallViewList;
    }
    for (Map<String, Object> tempMap : map) {
      Object obj = tempMap.get("queryFlag");
      if (obj == null) {
        continue;
      }
      ResellerOrderFundOverallView resellerOrderFundOverallView = new ResellerOrderFundOverallView();
      String queryFlag = (String) tempMap.get("queryFlag");
      String resellerNo = (String) tempMap.get("resellerNo");
      resellerOrderFundOverallView.setQueryFlag(queryFlag);
      resellerOrderFundOverallView.setResellerNo(resellerNo);
      resellerOrderFundOverallViewList.add(resellerOrderFundOverallView);
    }
    return resellerOrderFundOverallViewList;
  }

  private Long sumResellerOrderByQueryFlag(ResellerOrder resellerOrder, String startDate,
      String endDate) {
    QueryWrapper<ResellerOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.select("ifnull(sum(amount),0) as amount");
    if (StringUtils.isNotBlank(startDate)) {
      queryWrapper.ge("gmt_create", startDate);
    }
    if (StringUtils.isNotBlank(endDate)) {
      queryWrapper.le("gmt_create", endDate);
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      queryWrapper.eq("reseller_no", resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getOrderStatus())) {
      queryWrapper.eq("order_status", resellerOrder.getOrderStatus());
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      queryWrapper.eq("product_type", resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getQueryFlag())) {
      queryWrapper.eq("query_flag", resellerOrder.getQueryFlag());
    }
    Map<String, Object> map = resellerOrderService.getMap(queryWrapper);
    if (map == null || map.size() <= 0) {
      return 0L;
    }
    Object obj = map.get("amount");
    if (obj == null) {
      return 0L;
    }
    return ((BigDecimal) obj).longValue();
  }


}



