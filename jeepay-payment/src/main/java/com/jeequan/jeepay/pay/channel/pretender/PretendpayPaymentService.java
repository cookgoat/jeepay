package com.jeequan.jeepay.pay.channel.pretender;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRS;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.pretender.PretenderOrderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.core.model.params.prentender.PrentenderpayNormalMchParams;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("pretenderpayPaymentService")
public class PretendpayPaymentService extends AbstractPaymentService {

    @Autowired
    private PretenderOrderFactory pretenderOrderFactory;


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
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        PrentenderpayNormalMchParams prentenderpayNormalMchParams =(PrentenderpayNormalMchParams) mchAppConfigContext.getNormalMchParamsByIfCode(getIfCode());
        String serviceName = PaywayUtil.getRealPretenderService(payOrder.getWayCode(),prentenderpayNormalMchParams.getBizType(),prentenderpayNormalMchParams.getProductType());
        BaseRq baseRq = new BaseRq();
        baseRq.setChargeAmount(payOrder.getAmount());
        PretenderOrder pretenderOrder = pretenderOrderFactory.getInstance(serviceName).createOrder(baseRq);
        CommonPayDataRS commonPayDataRS = new CommonPayDataRS();
        commonPayDataRS.setPayUrl(pretenderOrder.getPayUrl());
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        commonPayDataRS.setChannelRetMsg(channelRetMsg);

        //放置 响应数据
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        return commonPayDataRS;
    }

}
