package com.jeequan.jeepay.pay.channel.pretender;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.core.utils.DateUtil;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.pay.server.MatchPayDtaRs;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import com.jeequan.jeepay.pay.channel.PretenderOrderMatcher;
import com.jeequan.jeepay.pay.pretender.PretenderOrderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import static com.jeequan.jeepay.core.constants.ApiCodeEnum.NO_RESELLER_ORDER;
import com.jeequan.jeepay.core.model.params.prentender.PrentenderpayNormalMchParams;
import static com.jeequan.jeepay.core.constants.ApiCodeEnum.SYS_OPERATION_FAIL_CREATE;

@Service
public class PretenderOrderMatcherImpl implements PretenderOrderMatcher {

  private static final Logger logger = LoggerFactory.getLogger(PretenderOrderMatcherImpl.class);

  @Autowired
  private PayOrderService payOrderService;

  @Autowired
  private ConfigContextService configContextService;

  @Autowired
  private PretenderOrderFactory pretenderOrderFactory;

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public MatchPayDtaRs matchOrder(String orderId) {
    MatchPayDtaRs matchPayDtaRs = new MatchPayDtaRs();
    if (StringUtils.isBlank(orderId)) {
      matchPayDtaRs.setCode("5001");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
    }
    String desOrderId = JeepayKit.aesDecode(orderId);
    PayOrder payOrder = payOrderService.queryOrder(desOrderId);
    if (payOrder == null) {
      matchPayDtaRs.setCode("5001");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    matchPayDtaRs.setPayType(payOrder.getWayCode());
    matchPayDtaRs
        .setMatchEndTime(DateUtil.addDate(new Date(), 0, 0, 0, 0, 0, 180, 0));
    matchPayDtaRs.setAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount()));
    matchPayDtaRs.setMchOrderNo(payOrder.getMchOrderNo());
    if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
      matchPayDtaRs.setCode("4007");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }

    if (payOrder.getState() == PayOrder.STATE_FAIL) {
      matchPayDtaRs.setCode("4008");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    if (payOrder.getState() == PayOrder.STATE_CANCEL || payOrder.getState() == PayOrder.STATE_CLOSED
        || payOrder.getState() == PayOrder.STATE_REFUND ||
        payOrder.getState() == PayOrder.STATE_INIT) {
      matchPayDtaRs.setCode("4003");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }

    if (payOrder.getState() == PayOrder.STATE_ING) {
      int count = resellerOrderService.count(
          ResellerOrder.gw().eq(ResellerOrder::getMatchOutTradeNo, payOrder.getPayOrderId())
              .eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.PAYING.getCode()));
      if (count > 0) {
        matchPayDtaRs.setCode("4005");
        return matchPayDtaRs;
      }
    }
    MchAppConfigContext mchAppConfigContext = configContextService
        .getMchAppConfigContext(payOrder.getMchNo(), payOrder.getAppId());
    PrentenderpayNormalMchParams prentenderpayNormalMchParams = (PrentenderpayNormalMchParams) mchAppConfigContext
        .getNormalMchParamsByIfCode(payOrder.getIfCode());
    String serviceName = PaywayUtil
        .getRealPretenderService(payOrder.getWayCode(), prentenderpayNormalMchParams.getBizType(),
            prentenderpayNormalMchParams.getProductType());
    BaseRq baseRq = new BaseRq();
    baseRq.setChargeAmount(payOrder.getAmount());
    PretenderOrder pretenderOrder = pretenderOrderFactory.getInstance(serviceName)
        .createOrder(baseRq);
    matchPayDtaRs.setPayUrl(pretenderOrder.getPayUrl());
    updateResellerOrderToPaying(payOrder, pretenderOrder);
    payOrder.setChannelOrderNo(pretenderOrder.getOutTradeNo());
    matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmSuccess(pretenderOrder.getOutTradeNo()));
    matchPayDtaRs.setCode("4004");
    payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
        .set(PayOrder::getChannelOrderNo, pretenderOrder.getOutTradeNo())
        .set(PayOrder::getResellerOrderNo, pretenderOrder.getMatchResellerOrderNo())
        .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId()));
    //放置 响应数据
    return matchPayDtaRs;
  }

  public void updateResellerOrderToPaying(PayOrder payOrder, PretenderOrder pretenderOrder) {
    ResellerOrder resellerOrder = queryResellerOrder(pretenderOrder.getMatchResellerOrderNo());
    ResellerOrder updateParam = new ResellerOrder();
    updateParam.setGmtPayingStart(new Date());
    updateParam.setOrderStatus(ResellerOrderStatusEnum.PAYING.getCode());
    updateParam.setGmtUpdate(new Date());
    updateParam.setMatchOutTradeNo(payOrder.getPayOrderId());
    updateParam.setId(resellerOrder.getId());
    boolean isSuccess = resellerOrderService.updateById(updateParam);
    if (!isSuccess) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.updateResellerOrderToPaying] failed update resellerOrder failed ,save pretender order failed,pretenderOrder={}",
          pretenderOrder);
      throw new BizException(SYS_OPERATION_FAIL_CREATE);
    }
  }

  private ResellerOrder queryResellerOrder(String orderNo) {
    ResellerOrder resellerOrder = resellerOrderService.getOne(ResellerOrder.gw().eq(ResellerOrder::getOrderNo, orderNo));
    if (resellerOrder == null) {
      throw new BizException(NO_RESELLER_ORDER);
    }
    return resellerOrder;
  }

}
