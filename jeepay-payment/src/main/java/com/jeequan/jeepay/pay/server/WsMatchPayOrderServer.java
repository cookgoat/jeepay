package com.jeequan.jeepay.pay.server;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.slf4j.Logger;
import java.io.IOException;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.OnError;
import org.slf4j.LoggerFactory;
import javax.websocket.Session;
import javax.websocket.OnMessage;
import com.alibaba.fastjson.JSON;
import javax.websocket.server.PathParam;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.core.RetryTemplate;
import javax.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Controller;
import com.jeequan.jeepay.pay.channel.PretenderOrderMatcher;
import org.springframework.beans.factory.annotation.Autowired;

@ServerEndpoint("/api/anon/ws/payOrder/{payOrderId}")
@Controller

public class WsMatchPayOrderServer {

  private final static Logger logger = LoggerFactory.getLogger(WsMatchPayOrderServer.class);

  //支付订单号
  private String payOrderId = "";

  private Session session;

  private static PretenderOrderMatcher pretenderOrderMatcher;

  private  static PayOrderService payOrderService;

  @Autowired
  public void setPretenderOrderMatcher(PretenderOrderMatcher pretenderOrderMatcher) {
    WsMatchPayOrderServer.pretenderOrderMatcher = pretenderOrderMatcher;
  }

  @Autowired
  public void setPayOrderService(PayOrderService payOrderService) {
    WsMatchPayOrderServer.payOrderService = payOrderService;
  }

  /**
   * 连接建立成功调用的方法
   */
  @OnOpen
  public void onOpen(Session session, @PathParam("payOrderId") String payOrderId) {
    try {
      //设置当前属性
      this.payOrderId = payOrderId;
      this.session = session;
    } catch (Exception e) {
      logger.error("WsMatchPayOrderServer监听异常,payOrderId[{}]", payOrderId, e);
    }
  }

  /**
   * 连接关闭调用的方法
   */
  @OnClose
  public void onClose() {
    payOrderId = "";
    logger.info("WsMatchPayOrderServer连接关闭,payOrderId[{}]", payOrderId);
  }

  /**
   * @param session
   * @param error
   */
  @OnError
  public void onError(Session session, Throwable error) {
    logger.error("ws发生错误", error);
  }

  /**
   * 实现服务器主动推送
   */
  @OnMessage
  public void sendMessage(String message) throws IOException, InterruptedException {
    logger.info("WsMatchPayOrderServer accept,message[{}]", message);
    if (StringUtils.isBlank(message)) {
      return;
    }
    MatchPayDtaRs matchPayDtaRs;
    String desOrderId = JeepayKit.aesDecode(message);
    PayOrder payOrder = payOrderService.queryOrder(desOrderId);
    matchPayDtaRs = new MatchPayDtaRs();
    matchPayDtaRs.setMchOrderNo(payOrder.getMchOrderNo());
    matchPayDtaRs.setAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount()));
    matchPayDtaRs.setPayType(payOrder.getWayCode());
    session.getBasicRemote().sendText(JSON.toJSONString(matchPayDtaRs));
    Object ans = new RetryTemplate() {
      @Override
      protected Object doBiz() {
        return pretenderOrderMatcher.matchOrder(message);
      }
    }.setRetryTime(3).setSleepTime(10000).execute();
    if (ans == null) {
      matchPayDtaRs = new MatchPayDtaRs();
      matchPayDtaRs.setCode("4009");
    } else {
      matchPayDtaRs = (MatchPayDtaRs) ans;
    }
    session.getBasicRemote().sendText(JSON.toJSONString(matchPayDtaRs));
    session.close();
  }

}
