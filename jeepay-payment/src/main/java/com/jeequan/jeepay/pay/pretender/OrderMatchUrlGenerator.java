package com.jeequan.jeepay.pay.pretender;

/**
 * @author axl rose
 * @date 2021/9/13
 */

public interface OrderMatchUrlGenerator {

    /**
     *
     * @param orderNo 平台订单号
     * @return 匹配页面地址
     */
    String generate(String orderNo);
}
