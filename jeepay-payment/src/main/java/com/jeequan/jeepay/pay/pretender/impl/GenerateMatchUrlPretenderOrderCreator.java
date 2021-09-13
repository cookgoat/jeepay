package com.jeequan.jeepay.pay.pretender.impl;

import org.springframework.stereotype.Service;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreator;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("generateMatchUrl")
public class GenerateMatchUrlPretenderOrderCreator implements PretenderOrderCreator {

    @Override
    public PretenderOrder createOrder(BaseRq baseRq) {
        PretenderOrder pretenderOrder = new PretenderOrder();
        return pretenderOrder.setPayUrl("www.baidu.com");
    }

}
