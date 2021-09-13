package com.jeequan.jeepay.pay.pretender.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.impl.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreator;
import com.jeequan.jeepay.pay.pretender.PretenderOrderFactory;
import com.jeequan.jeepay.pay.pretender.OrderAssociateMatcher;
import static com.jeequan.jeepay.core.constants.ApiCodeEnum.PARAMS_ERROR;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("orderAssociateMatcher")
public class OrderAssociateMatcherImpl implements OrderAssociateMatcher {

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private PretenderOrderFactory pretenderOrderFactory;

    /**
     * todo  add cache logic,channel_extra
     * @param platformOrderNo platform order no
     * @param productType 产品类型
     * @return
     */
    @Override
    public String matchOrder(String platformOrderNo, String productType) {
        checkParam(platformOrderNo);
        PayOrder  payOrder = queryPayOrder(platformOrderNo);
        PretenderOrderCreator pretenderOrderCreator = pretenderOrderFactory.getInstance(productType);
        BaseRq baseRq = new BaseRq();
        baseRq.setChargeAmount(payOrder.getAmount());
        PretenderOrder pretenderOrder = pretenderOrderCreator.createOrder(baseRq);
        payOrder.setChannelOrderNo(pretenderOrder.getOutTradeNo());
        return pretenderOrder.getPayUrl();
    }

    private void checkParam(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException(PARAMS_ERROR);
        }
    }

    private PayOrder queryPayOrder(String orderNo) {
        PayOrder payOrder = payOrderService.getOne(PayOrder.gw().eq(PayOrder::getMchOrderNo, orderNo));
        if (payOrder == null) {
            throw new BizException(PARAMS_ERROR);
        }
        return payOrder;
    }


}
