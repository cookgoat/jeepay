package com.jeequan.jeepay.pay.pretender.match;

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
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreatorFactory;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.NO_RESELLER_ORDER;

import com.jeequan.jeepay.core.model.params.prentender.PrentenderpayNormalMchParams;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.SYS_OPERATION_FAIL_CREATE;

/**
 * @author axl rose
 */
@Service
public class OrderAssociateMatcherImpl implements OrderAssociateMatcher {

  private static final Logger logger = LoggerFactory.getLogger(OrderAssociateMatcherImpl.class);

  /**
   * pay order service
   */
  @Autowired
  private PayOrderService payOrderService;

  /**
   * pay interface config context service
   */
  @Autowired
  private ConfigContextService configContextService;

  /**
   * pretender order creator factory
   */
  @Autowired
  private PretenderOrderCreatorFactory pretenderOrderCreatorFactory;

  /**
   * reseller order service
   */
  @Autowired
  private ResellerOrderService resellerOrderService;

  /**
   * pretender order service
   */
  @Autowired
  private PretenderOrderService pretenderOrderService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public MatchPayDtaRs matchOrder(String orderId) {
    MatchPayDtaRs matchPayDtaRs = new MatchPayDtaRs();
    //if order id param is blank,set 5001 code and return;
    if (StringUtils.isBlank(orderId)) {
      matchPayDtaRs.setCode("5001");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    //do aes decode orderId
    String desOrderId = JeepayKit.aesDecode(orderId);
    //query pay order by orderId
    PayOrder payOrder = payOrderService.queryOrder(desOrderId);
    //if pay order is not exist.set 5001 code
    if (payOrder == null) {
      matchPayDtaRs.setCode("5001");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    //set  pay order common info
    setPayOrderCommonInfo(matchPayDtaRs, payOrder);
    //if pay order is STATE_SUCCESS,just set 4007 code
    if (payOrder.getState() == PayOrder.STATE_SUCCESS) {
      matchPayDtaRs.setCode("4007");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    //if pay order STATE_FAIL ,just set 4008
    if (payOrder.getState() == PayOrder.STATE_FAIL) {
      matchPayDtaRs.setCode("4008");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    //if pay order is STATE_CANCEL,STATE_CLOSED,STATE_INIT,just set 4003 code
    if (payOrder.getState() == PayOrder.STATE_CANCEL || payOrder.getState() == PayOrder.STATE_CLOSED
        || payOrder.getState() == PayOrder.STATE_REFUND ||
        payOrder.getState() == PayOrder.STATE_INIT) {
      matchPayDtaRs.setCode("4003");
      matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmFail());
      return matchPayDtaRs;
    }
    // if pay order is STATE_ING,and exist PAYING reseller order,return to  exist matching pay url
    PretenderOrder pretenderOrder = pretenderOrderService.getOne(PretenderOrder.gw()
        .eq(PretenderOrder::getOutTradeNo, payOrder.getChannelOrderNo()));
    int count = resellerOrderService.count(
        ResellerOrder.gw().eq(ResellerOrder::getMatchOutTradeNo, payOrder.getPayOrderId())
            .eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.PAYING.getCode()));
    if (pretenderOrder != null && count > 0) {
      matchPayDtaRs.setCode("4004");
      matchPayDtaRs.setPayUrl(pretenderOrder.getPayUrl());
      return matchPayDtaRs;
    }

    //if is STATE_ING but did not match any,this is first match pay url action
    //get the pay app config context by pay order's appid
    MchAppConfigContext mchAppConfigContext = configContextService
        .getMchAppConfigContext(payOrder.getMchNo(), payOrder.getAppId());
    //get pay normal params form MchAppConfigContext by interfaceCode(ifCode)
    PrentenderpayNormalMchParams prentenderpayNormalMchParams = (PrentenderpayNormalMchParams) mchAppConfigContext
        .getNormalMchParamsByIfCode(payOrder.getIfCode());
    //get pretenderPayBizType  by wayCode and bizType and productType
    String pretenderPayBizType = PaywayUtil
        .getRealPretenderService(payOrder.getWayCode(), prentenderpayNormalMchParams.getBizType(),
            prentenderpayNormalMchParams.getProductType());

    /**
     * build charge base request,set charge amount
     */
    BaseRq baseRq = new BaseRq();
    baseRq.setChargeAmount(payOrder.getAmount());
    /**
     *get  pretenderOrderCreator by pretenderPayBizType,and invoke ,get a success pretender order
     */
     pretenderOrder = pretenderOrderCreatorFactory.getInstance(pretenderPayBizType)
        .createOrder(baseRq);
    //set payUrl from pretenderOrder
    matchPayDtaRs.setPayUrl(pretenderOrder.getPayUrl());
    //update reseller order  PAYING
    updateResellerOrderToPaying(payOrder, pretenderOrder);
    //set pay order channel orderNo with pretender orderNo
    updateChannelOrderAndResellerOrderNo(payOrder, pretenderOrder);
    //set success back info
    matchPayDtaRs.setChannelRetMsg(ChannelRetMsg.confirmSuccess(pretenderOrder.getOutTradeNo()));
    matchPayDtaRs.setCode("4004");
    //放置 响应数据
    return matchPayDtaRs;
  }

  /**
   * update pay order with channelOrder and reseller order
   *
   * @param payOrder
   * @param pretenderOrder
   */
  private void updateChannelOrderAndResellerOrderNo(PayOrder payOrder,
      PretenderOrder pretenderOrder) {
    PayOrder updatePayOrderParam = new PayOrder();
    payOrder.setChannelOrderNo(pretenderOrder.getOutTradeNo());
    updatePayOrderParam.setVersion(payOrder.getVersion());
    updatePayOrderParam.setChannelOrderNo(pretenderOrder.getOutTradeNo());
    updatePayOrderParam.setResellerOrderNo(pretenderOrder.getMatchResellerOrderNo());
    updatePayOrderParam.setPayOrderId(payOrder.getPayOrderId());
    boolean isSuc = payOrderService.updateById(updatePayOrderParam);
    if(!isSuc){
     throw  new BizException("match failed");
    }
  }

  private void setPayOrderCommonInfo(MatchPayDtaRs matchPayDtaRs, PayOrder payOrder) {
    matchPayDtaRs.setPayType(payOrder.getWayCode());
    matchPayDtaRs.setMatchEndTime(DateUtil.addDate(new Date(), 0, 0, 0, 0, 0, 180, 0));
    matchPayDtaRs.setAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount()));
    matchPayDtaRs.setMchOrderNo(payOrder.getMchOrderNo());
  }

  public void updateResellerOrderToPaying(PayOrder payOrder, PretenderOrder pretenderOrder) {
    ResellerOrder resellerOrder = queryResellerOrder(pretenderOrder.getMatchResellerOrderNo());
    //build update reseller order param
    ResellerOrder updateParam = new ResellerOrder();
    updateParam.setGmtPayingStart(new Date());
    updateParam.setOrderStatus(ResellerOrderStatusEnum.PAYING.getCode());
    updateParam.setGmtUpdate(new Date());
    updateParam.setMatchOutTradeNo(payOrder.getPayOrderId());
    updateParam.setId(resellerOrder.getId());
    updateParam.setVersion(resellerOrder.getVersion());
    //update reseller PAYING
    boolean isSuccess = resellerOrderService.updateById(updateParam);
    if (!isSuccess) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.updateResellerOrderToPaying] failed update resellerOrder failed ,save pretender order failed,pretenderOrder={}",
          pretenderOrder);
      throw new BizException(SYS_OPERATION_FAIL_CREATE);
    }
  }

  /**
   * query reseller order
   *
   * @param orderNo reseller orderno
   * @return ResellerOrder
   */
  private ResellerOrder queryResellerOrder(String orderNo) {
    ResellerOrder resellerOrder = resellerOrderService.getOne(
        ResellerOrder.gw().eq(ResellerOrder::getOrderNo, orderNo));
    if (resellerOrder == null) {
      throw new BizException(NO_RESELLER_ORDER);
    }
    return resellerOrder;
  }
}
