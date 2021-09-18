package com.jeequan.jeepay.mgr.ctrl.pretender.pretenderorder;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import com.jeequan.jeepay.core.constants.PretenderOrderStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * @author axl rose
 * @date 2021/9/18
 */
@RestController
@RequestMapping("/api/pretenderOrders")
@Slf4j
public class PretenderOrderController extends CommonCtrl {

  @Autowired
  private PretenderOrderService pretenderOrderService;

  @PreAuthorize("hasAnyAuthority('ENT_PRETENDER_ORDER_GROUP_LIST')")
  @GetMapping
  public ApiRes list() {

    PretenderOrder pretenderOrder = getObject(PretenderOrder.class);

    LambdaQueryWrapper<PretenderOrder> condition = PretenderOrder.gw();
    if (pretenderOrder.getPretenderAccountId() != null) {
      condition.eq(PretenderOrder::getPretenderAccountId, pretenderOrder.getPretenderAccountId());
    }
    if (StringUtils.isNotBlank(pretenderOrder.getBizType())) {
      condition.like(PretenderOrder::getBizType, pretenderOrder.getBizType());
    }
    if (StringUtils.isNotBlank(pretenderOrder.getProductType())) {
      condition.like(PretenderOrder::getProductType, pretenderOrder.getProductType());
    }
    if (pretenderOrder.getId() != null) {
      condition.eq(PretenderOrder::getId, pretenderOrder.getId());
    }

    if (StringUtils.isBlank(pretenderOrder.getOutTradeNo())) {
      condition.eq(PretenderOrder::getOutTradeNo, pretenderOrder.getOutTradeNo());
    }

    if (StringUtils.isBlank(pretenderOrder.getStatus())) {
      condition.eq(PretenderOrder::getStatus, pretenderOrder.getStatus());
    }

    if (StringUtils.isBlank(pretenderOrder.getPayWay())) {
      condition.eq(PretenderOrder::getPayWay, pretenderOrder.getPayWay());
    }

    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(PretenderOrder::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(PretenderOrder::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(PretenderOrder::getGmtCreate);
    IPage<ResellerOrder> pages = pretenderOrderService.page(getIPage(true), condition);
    return ApiRes.page(pages);
  }

  @PreAuthorize("hasAuthority('ENT_PRETENDER_ORDER_GROUP_VIEW')")
  @RequestMapping(value = "/{pretenderOrderId}", method = RequestMethod.GET)
  public ApiRes detail(@PathVariable("pretenderOrderId") Long pretenderOrderId) {
    PretenderOrder pretenderOrder = pretenderOrderService.getById(pretenderOrderId);
    if (pretenderOrder == null) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
    }
    return ApiRes.ok(pretenderOrder);
  }

  @PreAuthorize("hasAuthority('ENT_PRETENDER_ORDER_GROUP_SEARCH_BY_RESELLER_ORDER')")
  @RequestMapping(value = "/searchByResellerOrder/{resellerOrderNo}", method = RequestMethod.GET)
  public ApiRes searchByResellerOrderNo(@PathVariable("resellerOrderNo") String resellerOrderNo) {
    if (StringUtils.isBlank(resellerOrderNo)) {
      return ApiRes.fail(ApiCodeEnum.PARAMS_ERROR);
    }
    PretenderOrder pretenderOrder = pretenderOrderService.getOne(
        PretenderOrder.gw().eq(PretenderOrder::getMatchResellerOrderNo, resellerOrderNo)
            .eq(PretenderOrder::getStatus, PretenderOrderStatusEnum.FINISH), true);

    if (pretenderOrder == null) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
    }
    return ApiRes.ok(pretenderOrder);
  }


}
