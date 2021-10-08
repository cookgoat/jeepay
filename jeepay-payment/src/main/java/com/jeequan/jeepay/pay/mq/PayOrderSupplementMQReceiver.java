package com.jeequan.jeepay.pay.mq;

import lombok.extern.slf4j.Slf4j;
import com.jeequan.jeepay.core.entity.PayOrder;
import org.springframework.stereotype.Component;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.pay.service.PayOrderProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.components.mq.model.PayOrderSupplementMQ;
import com.jeequan.jeepay.components.mq.model.PayOrderSupplementMQ.MsgPayload;

@Slf4j
@Component
public class PayOrderSupplementMQReceiver implements PayOrderSupplementMQ.IMQReceiver{

  @Autowired
  private PayOrderService payOrderService;

  @Autowired private PayOrderProcessService payOrderProcessService;

  @Override
  public void receive(MsgPayload payload) {
    try {
      String payOrderId = payload.getPayOrderId();
      int currentCount = payload.getCount();
      log.info("接收订单查询通知MQ, payOrderId={}, count={}", payOrderId, currentCount);
      currentCount++ ;

      PayOrder payOrder = payOrderService.getById(payOrderId);
      if(payOrder == null) {
        log.warn("查询支付订单为空,payOrderId={}", payOrderId);
        return;
      }

      if(payOrder.getState() == PayOrder.STATE_SUCCESS) {
        log.warn("订单状态已成功,不需补单渠道.payOrderId={}", payOrderId);
        return;
      }
      payOrder.setState(PayOrder.STATE_SUCCESS);
      payOrderService.updateById(payOrder);
      payOrderProcessService.confirmSuccess(payOrder);
    }catch (Exception e) {
      log.error(e.getMessage());
      return;
    }
    }
}
