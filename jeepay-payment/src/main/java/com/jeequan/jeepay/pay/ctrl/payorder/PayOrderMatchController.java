package com.jeequan.jeepay.pay.ctrl.payorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;
import com.alibaba.fastjson.JSONObject;
import com.github.rholder.retry.Retryer;
import org.apache.commons.lang3.StringUtils;
import com.github.rholder.retry.RetryerBuilder;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.JeepayKit;
import java.util.concurrent.ExecutionException;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.RetryException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.pay.server.MatchPayDtaRs;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.pay.server.WsPayOrderMatchServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.pay.pretender.match.OrderAssociateMatcher;

/**
 * @author axl rose
 * @date 2021/10/5
 */
@Slf4j
@RestController
@RequestMapping("/api/anon/match")
public class PayOrderMatchController extends AbstractCtrl {

  private final static Logger logger = LoggerFactory.getLogger(PayOrderMatchController.class);

  @Autowired
  private OrderAssociateMatcher pretenderOrderMatcher;

  @Autowired
  private PayOrderService payOrderService;

  Retryer<MatchPayDtaRs> retryer = RetryerBuilder.<MatchPayDtaRs>newBuilder().retryIfException()
      .withWaitStrategy(
          WaitStrategies.fixedWait(10, TimeUnit.SECONDS))
      .withStopStrategy(StopStrategies.stopAfterAttempt(6)).build();

  @RequestMapping("/match")
  public void match() {
    //请求参数
    logger.info("[PayOrderMatchController.match] start");
    JSONObject params = getReqParamJSON();
    String payOrderId = params.getString("payOrderId");
    if (StringUtils.isBlank(payOrderId)) {
      logger.info("[PayOrderMatchController.match] payOrderId is null payOrderId={}", payOrderId);
      return;
    }
    String desOrderId = JeepayKit.aesDecode(payOrderId);
    PayOrder payOrder = getPayOrder(desOrderId);
    if (payOrder == null) {
      logger.info("[PayOrderMatchController.match] payOrder is not exist payOrderId={}",
          payOrderId);
      MatchPayDtaRs matchPayDtaRs = getBackFailedMatchPayDtaRs();
      WsPayOrderMatchServer.sendMsgByOrderId(payOrderId, JSONObject.toJSONString(matchPayDtaRs));
      return;
    }
    //send exist pay order id null;
    MatchPayDtaRs matchPayDtaRs = buildExistPayOrderInfo(payOrder);
    WsPayOrderMatchServer.sendMsgByOrderId(payOrderId, JSONObject.toJSONString(matchPayDtaRs));
    //invoke match order
    try {
      matchPayDtaRs = retryer.call(() -> pretenderOrderMatcher.matchOrder(desOrderId));
      WsPayOrderMatchServer.sendMsgByOrderId(payOrderId, JSONObject.toJSONString(matchPayDtaRs));
    } catch (ExecutionException e) {
      logger.info(
          "[PayOrderMatchController.match]  ExecutionException execution [pretenderOrderMatcher.matchOrder] failed payOrderId={}",
          desOrderId);
    } catch (RetryException e) {
      logger.info(
          "[PayOrderMatchController.match] RetryException execution [pretenderOrderMatcher.matchOrder] failed payOrderId={}",
          desOrderId);
    }
  }

  public PayOrder getPayOrder(String desPayOrderId) {
    PayOrder payOrder = payOrderService.queryOrder(desPayOrderId);
    return payOrder;
  }

  public MatchPayDtaRs buildExistPayOrderInfo(PayOrder payOrder) {
    MatchPayDtaRs matchPayDtaRs = new MatchPayDtaRs();
    matchPayDtaRs.setMchOrderNo(payOrder.getMchOrderNo());
    matchPayDtaRs.setAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount()));
    matchPayDtaRs.setPayType(payOrder.getWayCode());
    return matchPayDtaRs;
  }


  private MatchPayDtaRs getBackFailedMatchPayDtaRs() {
    MatchPayDtaRs matchPayDtaRs = new MatchPayDtaRs();
    matchPayDtaRs.setCode("5001");
    matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
    return matchPayDtaRs;
  }

}
