package com.jeequan.jeepay.pay.pretender;

import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;

/**
 * pretender order interface,do pretend order create
 *
 * @author axl rose
 */
public interface PretenderOrderCreator {

    /**
     *  use pretender account create a order ,and return the order info with json format
     * @param baseRq   the param of creat
     * @return R the result of create
     */
    PretenderOrder  createOrder(BaseRq  baseRq);

}
