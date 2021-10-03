package com.jeequan.jeepay.pay.channel.pretender;

import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg.ChannelState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRS;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreatorFactory;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreator;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.model.params.prentender.PrentenderpayNormalMchParams;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("pretenderpayPaymentService")
@Slf4j
public class PretendpayPaymentService extends AbstractPaymentService {

  private static final Logger logger = LoggerFactory.getLogger(PretendpayPaymentService.class);

  @Autowired
  private PretenderOrderCreatorFactory pretenderOrderFactory;

  @Autowired
  private SysConfigService sysConfigService;


  @Override
  public String getIfCode() {
    return CS.IF_CODE.PRETENDERPAY;
  }

  @Override
  public boolean isSupport(String wayCode) {
    return true;
  }

  @Override
  public String preCheck(UnifiedOrderRQ bizRQ, PayOrder payOrder) {
    //todo pre check,maybe put the pretender reseller and cookie check in there
    return null;
  }

  @Override
  @Transactional
  public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder,
      MchAppConfigContext mchAppConfigContext) throws Exception {
    PrentenderpayNormalMchParams prentenderpayNormalMchParams = (PrentenderpayNormalMchParams) mchAppConfigContext
        .getNormalMchParamsByIfCode(getIfCode());
    String serviceName = PaywayUtil
        .getRealPretenderService(payOrder.getWayCode(), prentenderpayNormalMchParams.getBizType(),
            prentenderpayNormalMchParams.getProductType());
    BaseRq baseRq = new BaseRq();
    baseRq.setChargeAmount(payOrder.getAmount());
    PretenderOrderCreator pretenderOrderCreator = pretenderOrderFactory.getInstance(serviceName);
    if(!pretenderOrderCreator.hasAvailablePretenderAccount(baseRq)){
      throw new BizException(ApiCodeEnum.NO_PRETENDER_ACCOUNT);

    }
    if(!pretenderOrderCreator.hasAvailableResellerOrder(baseRq)){
      throw new BizException(ApiCodeEnum.NO_RESELLER_ORDER);
    }
    //放置 响应数据
    CommonPayDataRS commonPayDataRS = new CommonPayDataRS();
    commonPayDataRS.setPayUrl(
        sysConfigService.getDBApplicationConfig().genMatchOrderUrl(payOrder.getPayOrderId()));
    ChannelRetMsg channelRetMsg = new ChannelRetMsg();
    channelRetMsg.setChannelState(ChannelState.WAITING);
    commonPayDataRS.setChannelRetMsg(channelRetMsg);
    return commonPayDataRS;
  }

}
