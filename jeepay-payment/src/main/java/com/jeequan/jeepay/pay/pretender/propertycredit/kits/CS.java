package com.jeequan.jeepay.pay.pretender.propertycredit.kits;

/**
 * @author axl rose
 * @date 2021/9/8
 */
public class CS {

    /**
     *
     */
    public interface PROPERTY_CREDIT_API_RESULT_CODE {
        /**
         * success of api resp
         */
        String SUCCESS_CODE = "00";
        /**
         * isPaying of api resp
         */
        String IS_PAYING = "01";


    }

    /**
     *
     */
    public interface PROPERTY_CREDIT_RECHARGE_ACCOUNT_TYPE {
        /**
         * mobile account type of recharge account type
         */
        String MOBILE_ACCOUNT_TYPE = "2";

        /**
         * brand  account type of recharge account type
         */
        String BRAND_ACCOUNT_TYPE = "1";
    }

    /**
     *
     */
    public interface PROPERTY_CREDIT_BRAND_IDS {
        /**
         * didi brand id
         */
        String DIDI = "10001";

        /**
         * JD brand id
         */
        String JD = "10002";

        /**
         * TMALL brand  ID
         */
        String TMALL = "10003";

        /**
         * CTRP  brand ID
         */
        String CTRP = "10004";


        /**
         * FRESHIPPO  brand ID
         */
        String FRESHIPPO = "10005";
    }

    /**
     *
     */
    public interface PROPERTY_CREDIT_GOODS_BRAND_CODE {
        /**
         * didi brand id
         */
        String DIDI = "DIDI";

        /**
         * JD brand id
         */
        String JD = "JD";

        /**
         * TMALL brand  ID
         */
        String TMALL = "TM";

        /**
         * CTRP  brand ID
         */
        String CTRP = "XC";

        /**
         * CTRP  brand ID
         */
        String FRESHIPPO = "HM";
    }


    public static final String GOODS_ID_BUILD_STR = "101";

    public static final String RECHARGE_MERCHANT_ID = "d984307c2629212e00b70faaa1d27c82";

    public static final String HOST = "api.zihexin.net";
    public static final String ORIGIN = "https://api.zihexin.net";
    public static final String IS_DISCOUNT = "1";



    public interface API {
        /**
         * base url of PropertyCredit api
         */
        public static final String API_BASE_URL = "https://api.zihexin.net";

        /**
         * recharge page url of PropertyCredit api
         */
        public static final String RECHARGE_PAGE = "/topup-merchant/common/topup";

        /**
         * recharge  account log url of PropertyCredit api
         */
        public static final String RECHARGE_ACCOUNT_LOG = "/topup-merchant/common/getrechargeaccountlog";


        /**
         * check  account whether is login url of PropertyCredit api
         */
        public static final String IS_LOGIN = "/topup-merchant/common/islogin";


        /**
         * send sms code url of PropertyCredit api
         */
        public static final String SEND_SMS_CODE = "/topup-merchant/common/sendsmscode";


        /**
         * login url of PropertyCredit api
         */
        public static final String LOGIN = "/topup-merchant/common/login";


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

}
