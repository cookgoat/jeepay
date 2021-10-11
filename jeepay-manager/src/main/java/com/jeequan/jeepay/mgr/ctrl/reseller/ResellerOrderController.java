package com.jeequan.jeepay.mgr.ctrl.reseller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.ExcelUtil;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.biz.ResellerOrderCounter;
import com.jeequan.jeepay.service.biz.fileentity.ResellerOrderExportEntity;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderFundOverallView;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderOverallView;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@RestController
@RequestMapping("/api/resellerOrders")
@Slf4j
public class ResellerOrderController extends CommonCtrl {

  @Autowired
  private ResellerOrderService resellerOrderService;

  private static SimpleDateFormat sdf = new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss"));

  @Autowired
  private ResellerOrderCounter resellerOrderCounter;

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_GROUP_LIST')")
  @GetMapping
  public ApiRes list() {

    ResellerOrder resellerOrder = getObject(ResellerOrder.class);

    LambdaQueryWrapper<ResellerOrder> condition = ResellerOrder.gw();
    if (StringUtils.isNotEmpty(resellerOrder.getOrderNo())) {
      condition.eq(ResellerOrder::getOrderNo, resellerOrder.getOrderNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      condition.eq(ResellerOrder::getResellerNo, resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      condition.like(ResellerOrder::getProductType, resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getChargeAccount())) {
      condition.like(ResellerOrder::getChargeAccount, resellerOrder.getChargeAccount());
    }
    if (resellerOrder.getAmount() != null) {
      condition.eq(ResellerOrder::getAmount,
          resellerOrder.getAmount());
    }
    if (StringUtils.isNotBlank(resellerOrder.getQueryFlag())) {
      condition.like(ResellerOrder::getQueryFlag, resellerOrder.getQueryFlag());
    }

    if (StringUtils.isNotBlank(resellerOrder.getOrderStatus())) {
      condition.eq(ResellerOrder::getOrderStatus, resellerOrder.getOrderStatus());
    }

    if (StringUtils.isNotBlank(resellerOrder.getMatchOutTradeNo())) {
      condition.eq(ResellerOrder::getMatchOutTradeNo, resellerOrder.getMatchOutTradeNo());
    }
    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(ResellerOrder::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(ResellerOrder::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(ResellerOrder::getId);
    IPage<ResellerOrder> pages = resellerOrderService.page(getIPage(true), condition);
    return ApiRes.page(pages);
  }


  @PreAuthorize("hasAuthority('ENT_RESELLER_ORDER_GROUP_VIEW')")
  @RequestMapping(value = "/{resellerOrderId}", method = RequestMethod.GET)
  public ApiRes detail(@PathVariable("resellerOrderId") Long resellerOrderId) {
    ResellerOrder resellerOrder = resellerOrderService.getById(resellerOrderId);
    if (resellerOrder == null) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
    }
    return ApiRes.ok(resellerOrder);
  }

  @PreAuthorize("hasAuthority('ENT_RESELLER_ORDER_GROUP_EDIT')")
  @PutMapping("/{resellerOrderId}")
  @MethodLog(remark = "更新订单")
  public ApiRes update(@PathVariable("resellerOrderId") Long resellerOrderId) {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    resellerOrder.setId(resellerOrderId);
    boolean result = resellerOrderService.updateById(resellerOrder);
    if (!result) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }
    return ApiRes.ok();
  }




  @PreAuthorize("hasAuthority('ENT_RESELLER_ORDER_GROUP_DELETE')")
  @DeleteMapping("/{resellerOrderId}")
  @MethodLog(remark = "删除订单")
  public ApiRes delete(@PathVariable("resellerOrderId") Long resellerOrderId) {
    int count = resellerOrderService.count(ResellerOrder.gw()
        .eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.FINISH)
        .eq(ResellerOrder::getId, resellerOrderId));
    if (count > 0) {
      throw new BizException("该订单已经支付完成");
    }
    boolean result = resellerOrderService.removeById(resellerOrderId);
    if (!result) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
    }
    return ApiRes.ok();
  }
  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_GROUP_EXPORT')")
  @GetMapping(value = "export")
  public void export(HttpServletResponse httpServletResponse) {

    ResellerOrder resellerOrder = getObject(ResellerOrder.class);

    LambdaQueryWrapper<ResellerOrder> condition = ResellerOrder.gw();
    if (StringUtils.isNotEmpty(resellerOrder.getOrderNo())) {
      condition.eq(ResellerOrder::getOrderNo, resellerOrder.getOrderNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      condition.eq(ResellerOrder::getResellerNo, resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      condition.like(ResellerOrder::getProductType, resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getChargeAccount())) {
      condition.like(ResellerOrder::getChargeAccount, resellerOrder.getChargeAccount());
    }
    if (resellerOrder.getAmount() != null) {
      condition.eq(ResellerOrder::getAmount,
          AmountUtil.convertDollar2Cent(resellerOrder.getAmount() + ""));
    }
    if (StringUtils.isNotBlank(resellerOrder.getQueryFlag())) {
      condition.like(ResellerOrder::getQueryFlag, resellerOrder.getQueryFlag());
    }

    if (StringUtils.isNotBlank(resellerOrder.getOrderStatus())) {
      condition.eq(ResellerOrder::getOrderStatus, resellerOrder.getOrderStatus());
    }

    if (StringUtils.isNotBlank(resellerOrder.getMatchOutTradeNo())) {
      condition.eq(ResellerOrder::getMatchOutTradeNo, resellerOrder.getMatchOutTradeNo());
    }
    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(ResellerOrder::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(ResellerOrder::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(ResellerOrder::getId);
    List<ResellerOrder> resellerOrderList = resellerOrderService.list(condition);
    if(resellerOrderList.size()>0){
      ExcelUtil.exportExcel(convertTo(resellerOrderList), "核销订单","核销订单",
          ResellerOrderExportEntity.class,getToday(new Date())+"-核销订单",httpServletResponse);
    }
  }


  List<ResellerOrderExportEntity> convertTo(List<ResellerOrder> resellerOrderList){
    List<ResellerOrderExportEntity> resellerOrderExportEntityList = new ArrayList<>();
    for(ResellerOrder resellerOrder:resellerOrderList){
      ResellerOrderExportEntity resellerOrderExportEntity = new ResellerOrderExportEntity();
      resellerOrderExportEntity.setResellerNo(resellerOrder.getResellerNo());
      resellerOrderExportEntity.setOrderNo(resellerOrder.getOrderNo());
      resellerOrderExportEntity.setOrderStatus(ResellerOrderStatusEnum.getType(resellerOrder.getOrderStatus()).getMsg());
      resellerOrderExportEntity.setAmount(AmountUtil.convertCent2Dollar(resellerOrder.getAmount()));
      resellerOrderExportEntity.setChargeAccount(resellerOrder.getChargeAccount());
      resellerOrderExportEntity.setProductType(
          ProductTypeEnum.getType(resellerOrder.getProductType()).getMsg());
      resellerOrderExportEntity.setGmtCreate(sdf.format(resellerOrder.getGmtCreate()));
      resellerOrderExportEntity.setGmtUpdate(sdf.format(resellerOrder.getGmtUpdate()));
      resellerOrderExportEntityList.add(resellerOrderExportEntity);
    }
    return resellerOrderExportEntityList;
  }


  public static String getToday(Date time){
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(time);
    return new SimpleDateFormat(("yyyy-MM-dd")).format(calendar.getTime());
  }

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_OVERALL_COUNT')")
  @GetMapping(value = "overallResellerCount")
  public ApiRes overallResellerCount() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JSONObject paramJSON = getReqParamJSON();
    String startStr = paramJSON.getString("createdStart");
    String createdEnd = paramJSON.getString("createdEnd");
    List<ResellerOrderOverallView> resellerOrderOverallViewList = resellerOrderCounter
        .countOverallViewByAmount(resellerOrder, startStr, createdEnd);
    return ApiRes.ok(resellerOrderOverallViewList);
  }


  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_OVERALL_COUNT')")
  @GetMapping(value = "orderFundOverallView")
  public ApiRes orderFundOverallView() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JSONObject paramJSON = getReqParamJSON();
    String startStr = paramJSON.getString("createdStart");
    String createdEnd = paramJSON.getString("createdEnd");
    List<ResellerOrderFundOverallView> resellerOrderOverallViewList = resellerOrderCounter
        .countReSellerOrderFundByReseller(startStr, createdEnd,
            resellerOrder);
    return ApiRes.ok(resellerOrderOverallViewList);
  }


  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_OVERALL_COUNT')")
  @GetMapping(value = "orderFundOverallViewByChildReseller")
  public ApiRes orderFundOverallViewByChildReseller() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JSONObject paramJSON = getReqParamJSON();
    String startStr = paramJSON.getString("createdStart");
    String createdEnd = paramJSON.getString("createdEnd");
    List<ResellerOrderFundOverallView> resellerOrderOverallViewList = resellerOrderCounter
        .countReSellerOrderFundByQueryFlag(startStr,createdEnd,resellerOrder);
    return ApiRes.ok(resellerOrderOverallViewList);
  }

}
