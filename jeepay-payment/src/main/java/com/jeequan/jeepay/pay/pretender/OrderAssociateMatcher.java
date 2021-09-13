package com.jeequan.jeepay.pay.pretender;

/**
 *
 * @author axl rose
 *
 */
public interface OrderAssociateMatcher {

    /**
     * match a reseller order and return a pay url
     * @param platformOrderNo platform order no
     * @param productType 产品类型
     * @return String Return pay url
     */
    String  matchOrder(String platformOrderNo,String productType);

}
