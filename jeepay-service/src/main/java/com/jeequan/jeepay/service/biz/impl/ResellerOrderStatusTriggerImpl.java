package com.jeequan.jeepay.service.biz.impl;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.service.biz.ResellerOrderStatusTrigger;
import com.jeequan.jeepay.service.biz.rq.ResellerOrderTriggerRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

/**
 * @author axl rose
 * @date 2021/10/11
 */

@Service
public class ResellerOrderStatusTriggerImpl implements ResellerOrderStatusTrigger {

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Override
  public void batchEnable(ResellerOrderTriggerRequest resellerOrderTriggerRequest) {
    checkParam(resellerOrderTriggerRequest);
    Set<Long> nulliftOrderIds = filterResellerIdByOpt(resellerOrderTriggerRequest,
        (resellerOrder -> StringUtils.equalsIgnoreCase(resellerOrder.getOrderStatus(),
            ResellerOrderStatusEnum.NULLIFY.getCode())));
    batchUpdateResellerOrders(nulliftOrderIds, ResellerOrderStatusEnum.WAIT_CHARGE);
  }

  private void checkParam(ResellerOrderTriggerRequest resellerOrderTriggerRequest) {
    if (resellerOrderTriggerRequest == null
        || null == resellerOrderTriggerRequest.getResellerOrderIds() ||
        resellerOrderTriggerRequest.getResellerOrderIds().size() == 0) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
  }


  private Set<Long> filterResellerIdByOpt(ResellerOrderTriggerRequest resellerOrderTriggerRequest,
      Predicate<ResellerOrder> predicate) {
    Set<Long> ids = new HashSet<>();
    List<ResellerOrder> resellerOrderList = queryResellerOrders(
        resellerOrderTriggerRequest.getResellerOrderIds());
    if (resellerOrderList.size() <= 0 || resellerOrderList == null) {
      return ids;
    }
    return resellerOrderList.stream().filter(predicate)
        .collect(Collectors.groupingBy(ResellerOrder::getId)).keySet();
  }


  private List<ResellerOrder> queryResellerOrders(List<Long> resellerOrderIds) {
    LambdaQueryWrapper<ResellerOrder> lambdaQueryWrapper = ResellerOrder.gw();
    lambdaQueryWrapper.select(ResellerOrder::getId, ResellerOrder::getOrderStatus)
        .in(ResellerOrder::getId, resellerOrderIds);
    return resellerOrderService.list(lambdaQueryWrapper);
  }

  private void batchUpdateResellerOrders(Set<Long> resellerOrderIds,
      ResellerOrderStatusEnum resellerOrderStatusEnum) {
    LambdaUpdateWrapper<ResellerOrder> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
    lambdaUpdateWrapper.set(ResellerOrder::getOrderStatus, resellerOrderStatusEnum.getCode());
    lambdaUpdateWrapper.in(ResellerOrder::getId, resellerOrderIds);
    resellerOrderService.update(lambdaUpdateWrapper);
  }

  @Override
  public void batchDisable(ResellerOrderTriggerRequest resellerOrderTriggerRequest) {
    checkParam(resellerOrderTriggerRequest);
    Set<Long> nulliftOrderIds = filterResellerIdByOpt(resellerOrderTriggerRequest,
        (resellerOrder -> StringUtils.equalsIgnoreCase(resellerOrder.getOrderStatus(),
            ResellerOrderStatusEnum.WAIT_CHARGE.getCode())));
    batchUpdateResellerOrders(nulliftOrderIds, ResellerOrderStatusEnum.NULLIFY);
  }

}
