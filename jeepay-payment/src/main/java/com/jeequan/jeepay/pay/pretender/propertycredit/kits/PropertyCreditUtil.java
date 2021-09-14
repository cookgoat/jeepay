package com.jeequan.jeepay.pay.pretender.propertycredit.kits;

import com.alibaba.fastjson.JSON;
import com.ejlchina.okhttps.FastjsonMsgConvertor;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.OkHttps;
import com.jeequan.jeepay.core.cookie.CookieJarImpl;
import com.jeequan.jeepay.core.cookie.MemoryCookieStore;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.JsoupUtils;
import com.jeequan.jeepay.core.utils.MapUtil;
import com.jeequan.jeepay.core.utils.OkHttpsHelp;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.BasePayRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.CreateOrderRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.GetRechargeAccountLogRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.QueryOrderRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.BaseResult;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.CreateOrderResult;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.QueryOrderResult;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.ToWechatPayResult;
import okhttp3.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS.*;
import static com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS.API.GO_PAY_ORDER_URL;
import static com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS.API.RECHARGE_PAGE;

/**
 * PropertyCredit pay service
 *
 * @author axl rose
 * @date 2021/9/7
 */
public class PropertyCreditUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertyCreditUtil.class);

    private static final String ALL_RECHARGE_PAGE = CS.API.API_BASE_URL + RECHARGE_PAGE + "?m=" + RECHARGE_MERCHANT_ID;


    /**
     * just request the recharge page
     *
     * @return String return recharge page
     */
    public static String requestRechargePage() {
        try {
            logger.info("[PropertyCreditUtil.requestRechargePage] start requestRechargePage");
            return JsoupUtils.httpGet(ALL_RECHARGE_PAGE, "");
        } catch (IOException e) {
            logger.error("[PropertyCreditUtil.requestRechargePage] request recharge page failed", e);
            return "";
        }
    }


    /**
     * just get recharge account log
     *
     * @param getRechargeAccountLogRequest get recharge account log param
     * @return String return recharge account log json
     */
    public static String getRechargeAccountLog(GetRechargeAccountLogRequest getRechargeAccountLogRequest) {
        logger.info("[PropertyCreditUtil.getRechargeAccountLog] start getRechargeAccountLog,getRechargeAccountLogRequest={}", JSON.toJSON(getRechargeAccountLogRequest));
        HTTP http = getBaseHttp();
        Map<String, Object> maps = JSON.parseObject(JSON.toJSONString(getRechargeAccountLogRequest));
        HttpResult httpResult = http.async(API.RECHARGE_ACCOUNT_LOG)
                .nothrow()
                .addBodyPara(maps)
                .addHeader(buildPropertyCreditCommonHeaders(getRechargeAccountLogRequest.getCookie()))
                .post().getResult();
        OkHttpsHelp<String> okHttpsHelp = new OkHttpsHelp<>();
        String result = okHttpsHelp.handleHttpResult(httpResult, true, String.class);
        logger.info("[PropertyCreditUtil.getRechargeAccountLog] ending getRechargeAccountLog,result={}", result);

        return result;
    }

    /**
     * query user whether is login
     *
     * @param cookie use cookie do log in action
     * @return BaseResult return rhe base result
     */
    public static BaseResult isLogin(String cookie) {
        logger.info("[PropertyCreditUtil.isLogin] start isLogin,cookie={}", cookie);
        HTTP http = getBaseHttp();
        Map<String, String> paramMap = new HashMap<>(1);
        paramMap.put("merchantId", RECHARGE_MERCHANT_ID);
        HttpResult httpResult = http.async(API.IS_LOGIN)
                .nothrow()
                .addBodyPara(paramMap)
                .addHeader(buildPropertyCreditCommonHeaders(cookie))
                .post().getResult();
        OkHttpsHelp<BaseResult> okHttpsHelp = new OkHttpsHelp<>();
        BaseResult result = okHttpsHelp.handleHttpResult(httpResult, false, BaseResult.class);
        logger.info("[PropertyCreditUtil.isLogin] ending isLogin,cookie={},result{}", cookie, result);
        return result;
    }


    /**
     * sendSmsCode
     *
     * @param phoneNo phone no use for send sms code
     * @return BaseResult base result
     */
    public static BaseResult sendSmsCode(String phoneNo) {
        logger.info("[PropertyCreditUtil.sendSmsCode] start sendSmsCode,phoneNo={}", phoneNo);
        HTTP http = getBaseHttp();
        Map<String, String> paramMap = new HashMap<>(1);
        paramMap.put("phone", phoneNo);
        HttpResult httpResult = http.async(API.SEND_SMS_CODE)
                .nothrow()
                .addBodyPara(paramMap)
                .addHeader(buildPropertyCreditCommonHeaders(null))
                .post().getResult();
        OkHttpsHelp<BaseResult> okHttpsHelp = new OkHttpsHelp<>();
        return okHttpsHelp.handleHttpResult(httpResult, false, BaseResult.class);
    }

    /**
     * do log in action,if success return the session cookie
     *
     * @param phoneNo phone no for log in
     * @return String return the log session cookie
     */
    public static String login(String phoneNo, String code) {
        logger.info("[PropertyCreditUtil.login] start login,phoneNo={},code={}", phoneNo, code);
        MemoryCookieStore memoryCookieStore = new MemoryCookieStore();
        HTTP http = HTTP.builder()
                .addMsgConvertor(new FastjsonMsgConvertor())
                .baseUrl(CS.API.API_BASE_URL)
                .bodyType(OkHttps.FORM)
                .config(aConfig -> aConfig.cookieJar(new CookieJarImpl(memoryCookieStore)))
                .build();
        Map<String, String> paramMap = new HashMap<>(3);
        paramMap.put("phone", phoneNo);
        paramMap.put("code", code);
        paramMap.put("merchantId", RECHARGE_MERCHANT_ID + "?phone=" + phoneNo);
        HttpResult httpResult = http.async(API.LOGIN)
                .nothrow()
                .addBodyPara(paramMap)
                .addHeader(buildPropertyCreditCommonHeaders(null))
                .post().getResult();
        OkHttpsHelp<BaseResult> okHttpsHelp = new OkHttpsHelp<>();
        BaseResult baseResult = okHttpsHelp.handleHttpResult(httpResult, false, BaseResult.class);
        if (!baseResult.isSuccess()) {
            logger.info("[PropertyCreditUtil.login]  login failed,phoneNo={},code={},baseResult={}", phoneNo, code, JSON.toJSONString(baseResult));
            throw new BizException("PropertyCreditUtil.login failed");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Cookie cookie : memoryCookieStore.getCookies()) {
            stringBuilder.append(cookie.name()).append("=");
            stringBuilder.append(cookie.value());
            stringBuilder.append(";");
        }
        String value = stringBuilder.substring(0, stringBuilder.lastIndexOf(";"));
        logger.info("[PropertyCreditUtil.login]  login success return cookie={},phoneNo={},code={},baseResult={}", value, phoneNo, code, JSON.toJSONString(baseResult));
        return value;
    }

    /**
     * createOrder ,create a card order
     *
     * @param createOrderRequest create order request
     * @return CreateOrderResult order info
     */
    public static CreateOrderResult createOrder(CreateOrderRequest createOrderRequest) {
        logger.info("[PropertyCreditUtil.createOrder] start createOrder,createOrderRequest={}", JSON.toJSONString(createOrderRequest));
        HTTP http = getBaseHttp();
        Map<String, Object> paramMap = MapUtil.objectToMap(createOrderRequest);
        HttpResult result = http.async(API.CREATE_ORDER_URL)
                .nothrow()
                .addBodyPara(paramMap)
                .addHeader(buildPropertyCreditCommonHeaders(createOrderRequest.getCookie()))
                .post().getResult();
        OkHttpsHelp<CreateOrderResult> okHttpsHelp = new OkHttpsHelp<>();
        CreateOrderResult createOrderResult = okHttpsHelp.handleHttpResult(result, false, CreateOrderResult.class);
        if (!createOrderResult.isSuccess()) {
            logger.error("[PropertyCreditUtil.createOrder]invoke PropertyCredit failed,createOrderRequest={},CreateOrderResult={}",
                    JSON.toJSONString(createOrderRequest), JSON.toJSONString(createOrderResult));
            throw new BizException("PropertyCreditUtil.createOrder failed");
        }
        logger.info("[PropertyCreditUtil.createOrder]invoke PropertyCredit success,createOrderRequest={},CreateOrderResult={}",
                JSON.toJSONString(createOrderRequest), JSON.toJSONString(createOrderResult));
        return createOrderResult;
    }

    /**
     * do goPayAction,just for pretend
     *
     * @param basePayRequest go pay request
     * @return String return the pay page
     */
    public static String goPay(BasePayRequest basePayRequest) {
        String requestJsonString = JSON.toJSONString(basePayRequest);
        logger.info("[PropertyCreditUtil.goPay] start goPayRequest={}", requestJsonString);
        HTTP http = getBaseHttp();
        Map<String, String> paramMap = MapUtil.convertToMap(requestJsonString);
        Map<String, String> headerMap = buildPropertyCreditCommonHeaders(basePayRequest.getCookie());
        headerMap.put("Referer", "https://api.zihexin.net/topup-merchant/common/topup?m=d984307c2629212e00b70faaa1d27c82");
        HttpResult httpResult = http.async(GO_PAY_ORDER_URL)
                .nothrow()
                .addUrlPara(paramMap)
                .addHeader(headerMap)
                .get().getResult();
        OkHttpsHelp<String> okHttpsHelp = new OkHttpsHelp<>();
        return okHttpsHelp.handleHttpResult(httpResult, true, String.class);
    }


    /**
     * do goPayAction,just for pretend
     *
     * @param basePayRequest to alipay param
     * @return String  json
     */
    public static String toAlipay(BasePayRequest basePayRequest) {
        String requestJsonString = JSON.toJSONString(basePayRequest);
        logger.info("[PropertyCreditUtil.toAlipayRequest] start toAlipayRequest={}", requestJsonString);
        HTTP http = getBaseHttp();
        Map<String, String> paramMap = MapUtil.convertToMap(requestJsonString);
        Map<String, String> headerMap = buildPropertyCreditCommonHeaders(basePayRequest.getCookie());
        headerMap.put("Referer", "https://api.zihexin.net/topup-merchant/common/topup?m=d984307c2629212e00b70faaa1d27c82");
        HttpResult httpResult = http.async(API.TO_ALIPAY)
                .nothrow()
                .addUrlPara(paramMap)
                .addHeader(headerMap)
                .get().getResult();
        OkHttpsHelp<String> okHttpsHelp = new OkHttpsHelp<>();
        logger.info("[PropertyCreditUtil.toAlipayRequest] success httpResult={}", httpResult);
        return okHttpsHelp.handleHttpResult(httpResult, true, String.class);
    }

    private static HTTP getBaseHttp() {
        return HTTP.builder()
                .addMsgConvertor(new FastjsonMsgConvertor())
                .baseUrl(API.API_BASE_URL)
                .bodyType(OkHttps.FORM)
                .build();
    }

    /**
     * do post alipay ,and get alipay url
     *
     * @param paramMap param
     * @return String url for do  pay
     */
    public static String postAlipay(Map<String, String> paramMap) {
        String requestJsonString = JSON.toJSONString(paramMap);
        logger.info("[PropertyCreditUtil.postAlipay] start paramMap={}", requestJsonString);
        HTTP http = HTTP.builder()
                .addMsgConvertor(new FastjsonMsgConvertor())
                .bodyType(OkHttps.FORM)
                .config(a -> a.followRedirects(false))
                .build();
        String url = paramMap.get("action");
        paramMap.remove("action");
        OkHttpsHelp<String> okHttpsHelp = new OkHttpsHelp<>();
        HttpResult httpResult = http.async(url).addBodyPara(paramMap).post().getResult();
        okHttpsHelp.handleHttpResult(httpResult, true, String.class);
        String result = httpResult.getHeader("Location");
        logger.info("[PropertyCreditUtil.postAlipay] success result={}", result);
        return result;
    }

    /**
     * to weChatPay,get weChat pay json
     *
     * @param basePayRequest to we chat pay request
     * @return String ToWechatPayResult
     */
    public static ToWechatPayResult toWechatPay(BasePayRequest basePayRequest) {
        String requestJsonString = JSON.toJSONString(basePayRequest);
        logger.info("[PropertyCreditUtil.toWechatPay] start toWechatPayRequest={}", requestJsonString);
        Map<String, String> paramMap = MapUtil.convertToMap(requestJsonString);
        Map<String, String> headersMap = HeaderHelper.buildCommonHeaders(basePayRequest.getCookie());
        enCodeHeadersMap(headersMap);
        HTTP http = getBaseHttp();
        HttpResult httpResult = http.async(API.TO_WECHAT)
                .nothrow()
                .addHeader(headersMap)
                .addUrlPara(paramMap)
                .post().getResult();
        OkHttpsHelp<ToWechatPayResult> okHttpsHelp = new OkHttpsHelp<>();
        ToWechatPayResult toWechatPayResult = okHttpsHelp.handleHttpResult(httpResult, false, ToWechatPayResult.class);
        if (toWechatPayResult == null || !toWechatPayResult.isSuccess()) {
            logger.error("[PropertyCreditUtil.toWechatPay] failed httpResult={}", httpResult);
            throw new BizException("toWechatPay failed");
        }
        return toWechatPayResult;
    }


    /**
     * post postWechatPay and get deeplink pay url return
     *
     * @param mWebWechatUrl we chat pay mWeb url
     * @return String wechat deeplink url ,e.g:wx://
     */
    public static String postWechatPay(String mWebWechatUrl) {
        logger.info("[PropertyCreditUtil.toWechatPay] start postWechatPay={}", mWebWechatUrl);
        Map<String, String> headersMap = HeaderHelper.buildCommonHeaders(null);
        headersMap.put("Host", "wx.tenpay.com");
        headersMap.put("Referer", "https://api.zihexin.net/");
        try {
            return JsoupUtils.httpGet(mWebWechatUrl, headersMap);
        } catch (Exception e) {
            logger.error("[PropertyCreditUtil.postWechatPay] failed mWebWechatUrl={},headersMap={},e={}", mWebWechatUrl, headersMap, e);
            throw new BizException("PropertyCreditUtil.toWechatPay exception");
        }
    }


    /**
     * query order
     *
     * @param queryOrderRequest query order request
     * @return QueryOrderResult order info
     */
    public static QueryOrderResult queryOrder(QueryOrderRequest queryOrderRequest) {
        logger.info("[PropertyCreditUtil.queryOrder] start queryOrder,queryOrderRequest={}", JSON.toJSONString(queryOrderRequest));
        HTTP http = getBaseHttp();
        Map<String, Object> paramMap = MapUtil.objectToMap(queryOrderRequest);
        HttpResult result = http.async(API.QUERY_ORDER)
                .nothrow()
                .addBodyPara(paramMap)
                .addHeader(buildPropertyCreditCommonHeaders(queryOrderRequest.getCookie()))
                .post().getResult();
        OkHttpsHelp<QueryOrderResult> okHttpsHelp = new OkHttpsHelp<>();
        QueryOrderResult queryOrderResult = okHttpsHelp.handleHttpResult(result, false, QueryOrderResult.class);
        if (!queryOrderResult.isSuccess()) {
            logger.error("[PropertyCreditUtil.queryOrderResult]invoke PropertyCredit failed,queryOrderRequest={},queryOrderResult={}",
                    JSON.toJSONString(queryOrderRequest), JSON.toJSONString(queryOrderResult));
            throw new BizException("PropertyCreditUtil.createOrder failed");
        }
        logger.info("[PropertyCreditUtil.createOrder]invoke PropertyCredit success,queryOrderRequest={},queryOrderResult={}",
                JSON.toJSONString(queryOrderRequest), JSON.toJSONString(queryOrderResult));
        return queryOrderResult;
    }


    private static void enCodeHeadersMap(Map<String, String> headersMap) {
        headersMap.replaceAll((k, v) -> UrlParamHelper.encodeHeadInfo(headersMap.get(k)));
    }


    private static Map<String, String> buildPropertyCreditCommonHeaders(String cookie) {
        Map<String, String> headers = HeaderHelper.buildCommonHeaders(cookie);
        headers.put("Origin", ORIGIN);
        return headers;
    }

}
