package com.jeequan.jeepay.pay.channel.pretender;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.params.prentender.PrentenderpayNormalMchParams;
import com.jeequan.jeepay.pay.channel.AbstractPaymentService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.pretender.PretenderOrderFactory;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRS;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.util.PaywayUtil;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.SYS_OPERATION_FAIL_CREATE;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("pretenderpayPaymentService")
@Slf4j
public class PretendpayPaymentService extends AbstractPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PretendpayPaymentService.class);

    @Autowired
    private PretenderOrderFactory pretenderOrderFactory;

    @Autowired
    private ResellerOrderService resellerOrderService;

    @Autowired
    private PayOrderService payOrderService;

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
    public AbstractRS pay(UnifiedOrderRQ bizRQ, PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        PrentenderpayNormalMchParams prentenderpayNormalMchParams = (PrentenderpayNormalMchParams) mchAppConfigContext.getNormalMchParamsByIfCode(getIfCode());
        String serviceName = PaywayUtil.getRealPretenderService(payOrder.getWayCode(), prentenderpayNormalMchParams.getBizType(), prentenderpayNormalMchParams.getProductType());
        BaseRq baseRq = new BaseRq();
        baseRq.setChargeAmount(payOrder.getAmount());
        PretenderOrder pretenderOrder = pretenderOrderFactory.getInstance(serviceName).createOrder(baseRq);
        CommonPayDataRS commonPayDataRS = new CommonPayDataRS();
        commonPayDataRS.setPayUrl(pretenderOrder.getPayUrl());
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        commonPayDataRS.setChannelRetMsg(channelRetMsg);
        updateResellerOrderToPaying(payOrder, pretenderOrder);
        payOrder.setChannelOrderNo(pretenderOrder.getOutTradeNo());
        payOrderService.update(new LambdaUpdateWrapper<PayOrder>().set(PayOrder::getChannelOrderNo, pretenderOrder.getOutTradeNo())
                .eq(PayOrder::getPayOrderId, payOrder.getPayOrderId()));
        //放置 响应数据
        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
        return commonPayDataRS;
    }

    public void updateResellerOrderToPaying (PayOrder payOrder, PretenderOrder pretenderOrder){
        boolean isSuccess = resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>()
                .set(ResellerOrder::getGmtPayingStart, new Date())
                .set(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.PAYING.getCode())
                .set(ResellerOrder::getGmtUpdate, new Date())
                .set(ResellerOrder::getMatchOutTradeNo, payOrder.getMchOrderNo())
                .eq(ResellerOrder::getOrderNo, pretenderOrder.getMatchResellerOrderNo()));
        if (!isSuccess) {
            logger.error("[AbstractPropertyCreditOrderCreator.updateResellerOrderToPaying] failed update resellerOrder failed ,save pretender order failed,pretenderOrder={}", pretenderOrder);
            throw new BizException(SYS_OPERATION_FAIL_CREATE);
        }
    }


}
