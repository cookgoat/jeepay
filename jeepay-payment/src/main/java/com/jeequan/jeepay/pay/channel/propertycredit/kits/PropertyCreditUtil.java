package com.jeequan.jeepay.pay.channel.propertycredit.kits;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.alibaba.fastjson.JSONPatch.OperationType.add;

/**
 * PropertyCredit pay service
 * @author axl rose
 * @date 2021/9/7
 */
public class PropertyCreditUtil {

    /**
     * base url of PropertyCredit api
     */
    public static final String API_BASE_URL = "https://api.zihexin.net";

    /**
     * the url of create_order
     */
    public static final String CREATE_ORDER_URL = "/topup-merchant/common/createorder";

    /**
     * the url  to pay order page ,just simulate ,no other useful
     */
    public static final String GO_PAY_ORDER_URL = "/topup-merchant/common/topayorder";

    /**
     * the url to pay way of alipay
     */
    public static final String TO_ALIPAY = "/topup-merchant/common/toalipay";

    /**
     * the url to pay way of the  weChat
     */
    public static final String TO_WECHAT = "/topup-merchant/common/towechatpay";


    /**
     * the url of query orders
     */
    public static final String QUERY_ORDER = "/topup-merchant/common/queryorder";





}
