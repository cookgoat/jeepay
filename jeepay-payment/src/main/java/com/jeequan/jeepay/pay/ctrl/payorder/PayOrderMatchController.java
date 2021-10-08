package com.jeequan.jeepay.pay.ctrl.payorder;

import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.DateUtil;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Date;
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
import com.jeequan.jeepay.pay.server.MatchPayDtaRs;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.pay.server.WsPayOrderMatchServer;
import org.springframework.web.bind.annotation.PostMapping;
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
public class PayOrderMatchController extends ApiController {

  private final static Logger logger = LoggerFactory.getLogger(PayOrderMatchController.class);

  @Autowired
  private OrderAssociateMatcher pretenderOrderMatcher;

  @Autowired
  private PayOrderService payOrderService;

  Retryer<MatchPayDtaRs> retryer = RetryerBuilder.<MatchPayDtaRs>newBuilder().retryIfException()
      .withWaitStrategy(
          WaitStrategies.fixedWait(10, TimeUnit.SECONDS))
      .withStopStrategy(StopStrategies.stopAfterAttempt(6)).build();

  @PostMapping("/api/order/match")
  public ApiRes match(@Param("payOrderId") String payOrderId) {
    //请求参数
    logger.info("[PayOrderMatchController.match] start");
    if (StringUtils.isBlank(payOrderId)) {
      logger.info("[PayOrderMatchController.match] payOrderId is null payOrderId={}", payOrderId);
      return ApiRes.ok();
    }
    String desOrderId = JeepayKit.aesDecode(payOrderId);
    PayOrder payOrder = getPayOrder(desOrderId);
    if (payOrder == null) {
      logger.info("[PayOrderMatchController.match] payOrder is not exist payOrderId={}",
          payOrderId);
      MatchPayDtaRs matchPayDtaRs = getBackFailedMatchPayDtaRs();
      WsPayOrderMatchServer.sendMsgByOrderId(payOrderId, JSONObject.toJSONString(matchPayDtaRs));
      return ApiRes.ok();
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
    return  ApiRes.ok();
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
    matchPayDtaRs.setMatchEndTime(DateUtil.addDate(new Date(), 0, 0, 0, 0, 0, 120, 0));
    return matchPayDtaRs;
  }


  private MatchPayDtaRs getBackFailedMatchPayDtaRs() {
    MatchPayDtaRs matchPayDtaRs = new MatchPayDtaRs();
    matchPayDtaRs.setCode("5001");
    matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
    return matchPayDtaRs;
  }





}
