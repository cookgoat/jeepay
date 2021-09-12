package com.jeequan.jeepay.pay.pretender.impl;

import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.pretender.AbstractPretenderCreator;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreator;
import com.jeequan.jeepay.pay.pretender.cs.BizTypeEnum;
import com.jeequan.jeepay.pay.pretender.cs.PretenderOrderStatusEnum;
import com.jeequan.jeepay.pay.pretender.model.FacePrice;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.AlipayHelper;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.PropertyCreditUtil;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.StringFinder;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.BasePayRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.CreateOrderRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.CreateOrderResult;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.GetRechargeAccountLogRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.BaseResult;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.ToWechatPayResult;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.*;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public abstract class AbstractPropertyCreditOrderCreator extends AbstractPretenderCreator {

    private static final Logger logger = LoggerFactory.getLogger(PretenderOrderCreator.class);

    @Override
    protected boolean extendParamCheck(BaseRq baseRq) {
        //no other param check
        return true;
    }

    @Override
    protected String getBizType() {
        return BizTypeEnum.PROPERTY_CREDIT.getCode();
    }


    @Override
    protected PretenderOrder doCreateOrder(ResellerOrder resellerOrder, PretenderAccount pretenderAccount, FacePrice facePrice) {
        doCommonSimulateAction(pretenderAccount, facePrice);
        return doCreatePretenderOrder(resellerOrder, pretenderAccount, facePrice);
    }

    private void doCommonSimulateAction(PretenderAccount pretenderAccount, FacePrice facePrice) {
        //request recharge page,just for simulate
        PropertyCreditUtil.requestRechargePage();
        //check the pretender account whether login
        checkPretenderAccountWhetherLogin(pretenderAccount);
        //get the recharge account log,just for simulate
        GetRechargeAccountLogRequest getRechargeAccountLogRequest = new GetRechargeAccountLogRequest();
        getRechargeAccountLogRequest.setCookie(pretenderAccount.getCertificate());
        getRechargeAccountLogRequest.setRechargeProduct(facePrice.getProductCode());
        PropertyCreditUtil.getRechargeAccountLog(getRechargeAccountLogRequest);
    }


    private PretenderOrder doCreatePretenderOrder(ResellerOrder resellerOrder, PretenderAccount pretenderAccount, FacePrice facePrice) {
        //create order request
        CreateOrderRequest createOrderRequest = buildCreateOrderRequest(resellerOrder, pretenderAccount, facePrice);
        CreateOrderResult createOrderResult = PropertyCreditUtil.createOrder(createOrderRequest);
        if (!createOrderResult.isSuccess()) {
            logger.error("[AbstractPropertyCreditOrderCreator.doCreatePretenderOrder] failed,pretenderAccount={},createOrderRequest={},createOrderResult={}",
                    pretenderAccount,
                    createOrderRequest, createOrderResult);
            throw new BizException(PRETENDER_ACCOUNT_IS_NOT_LOGIN);
        }
        BasePayRequest basePayRequest = buildBasePayRequest(createOrderRequest, createOrderResult);
        PropertyCreditUtil.goPay(basePayRequest);
        String payUrl;
        if (StringUtils.equalsIgnoreCase(getPayWay(), com.jeequan.jeepay.core.constants.CS.PAY_WAY_CODE.ALI_WAP)) {
            payUrl = getAlipayUrl(basePayRequest);
        } else {
            payUrl = getWechatUrl(basePayRequest);
        }
        return savePretenderOrder(resellerOrder, pretenderAccount, createOrderResult, payUrl);
    }


    private CreateOrderRequest buildCreateOrderRequest(ResellerOrder resellerOrder, PretenderAccount pretenderAccount, FacePrice facePrice) {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCookie(pretenderAccount.getCertificate());
        createOrderRequest.setGoodsId(facePrice.getGoodsId());
        createOrderRequest.setRechargeProduct(facePrice.getProductCode());
        createOrderRequest.setRechargeAccount(pretenderAccount.getAccount());
        createOrderRequest.setRechargeType(facePrice.getGoodType());
        Long discount = AmountUtil.calPercentageFee(resellerOrder.getAmount(),
                BigDecimal.valueOf(facePrice.getDiscount()).divide(new BigDecimal(100),6, RoundingMode.HALF_UP));
        Long amount = resellerOrder.getAmount() + discount;
        String amountDollar = AmountUtil.convertCent2Dollar(amount);
        createOrderRequest.setRechargeAmount(amountDollar);
        return createOrderRequest;
    }


    private BasePayRequest buildBasePayRequest(CreateOrderRequest createOrderRequest, CreateOrderResult createOrderResult) {
        BasePayRequest basePayRequest = new BasePayRequest();
        basePayRequest.setCookie(createOrderRequest.getCookie());
        basePayRequest.setRechargeAccount(createOrderRequest.getRechargeAccount());
        basePayRequest.setRechargeType(createOrderRequest.getRechargeType());
        basePayRequest.setIsPaying(CS.PROPERTY_CREDIT_API_RESULT_CODE.IS_PAYING);
        basePayRequest.setPayAmount(createOrderRequest.getRechargeAmount());
        basePayRequest.setOrderNo(createOrderResult.getOrderNo());
        basePayRequest.setRechargeAmount(createOrderRequest.getRechargeAmount());
        basePayRequest.setGoodsName(getProductTypeEnum().getMsg() + createOrderRequest.getRechargeAmount() + "元");
        return basePayRequest;
    }


    private void checkPretenderAccountWhetherLogin(PretenderAccount pretenderAccount) {
        BaseResult baseResult = PropertyCreditUtil.isLogin(pretenderAccount.getCertificate());
        if (!baseResult.isSuccess()) {
            logger.error("[AbstractPropertyCreditOrderCreator.checkPretenderAccountWhetherLogin] is not login,pretenderAccount={}", pretenderAccount);
            throw new BizException(PRETENDER_ACCOUNT_IS_NOT_LOGIN);
        }
    }

    private String getAlipayUrl(BasePayRequest basePayRequest) {
        String alipayJsonData = PropertyCreditUtil.toAlipay(basePayRequest);
        if (StringUtils.isBlank(alipayJsonData)) {
            logger.error("[AbstractPropertyCreditOrderCreator.getAlipayUrl] to alipay failed,basePayRequest={},alipayJsonData={}", basePayRequest, alipayJsonData);
            throw new BizException(TO_ALIPAY_FAILED);
        }
        Map<String, String> alipayParams = AlipayHelper.getAlipayUrlFromAction(alipayJsonData);
        String payUrl = PropertyCreditUtil.postAlipay(alipayParams);
        if (StringUtils.isBlank(payUrl)) {
            logger.error("[AbstractPropertyCreditOrderCreator.getAlipayUrl] postAlipay failed,basePayRequest={},payUrl={}", basePayRequest, payUrl);
            throw new BizException(GET_ALIPAY_FAILED);
        }
        return payUrl;
    }


    private String getWechatUrl(BasePayRequest basePayRequest) {
        ToWechatPayResult toWechatPayResult = PropertyCreditUtil.toWechatPay(basePayRequest);
        if (!toWechatPayResult.isSuccess()) {
            logger.error("[AbstractPropertyCreditOrderCreator.getAlipayUrl] to alipay failed,basePayRequest={},toWechatPayResult={}", basePayRequest, toWechatPayResult);
            throw new BizException(TO_ALIPAY_FAILED);
        }
        String pageString = PropertyCreditUtil.postWechatPay(toWechatPayResult.getData());
        if (StringUtils.isBlank(pageString)) {
            logger.error("[AbstractPropertyCreditOrderCreator.getAlipayUrl] postAlipay failed,basePayRequest={},pageString={}", basePayRequest, pageString);
            throw new BizException(GET_ALIPAY_FAILED);
        }
        String payUrl = StringFinder.findWeChatDeeplinkFromString(pageString);
        if (StringUtils.isBlank(payUrl)) {
            logger.error("[AbstractPropertyCreditOrderCreator.getAlipayUrl] postAlipay failed,payUrl is blank,basePayRequest={},pageString={},payUrl={}", basePayRequest, pageString, payUrl);
            throw new BizException(GET_ALIPAY_FAILED);
        }
        return payUrl;
    }

    private PretenderOrder savePretenderOrder(ResellerOrder resellerOrder, PretenderAccount pretenderAccount, CreateOrderResult createOrderResult, String payUrl) {
        PretenderOrder pretenderOrder = new PretenderOrder();
        pretenderOrder.setAmount(resellerOrder.getAmount());
        pretenderOrder.setGmtCreate(new Date());
        pretenderOrder.setBizType(getBizType());
        pretenderOrder.setPretenderAccountId(pretenderAccount.getId());
        pretenderOrder.setMatchResellerOrderNo(resellerOrder.getOrderNo());
        pretenderOrder.setOutTradeNo(createOrderResult.getOrderNo());
        pretenderOrder.setPayUrl(payUrl);
        pretenderOrder.setPayWay(getPayWay());
        pretenderOrder.setStatus(PretenderOrderStatusEnum.PAYING.getCode());
        boolean isSaveSuccess = pretenderOrderService.save(pretenderOrder);
        if (!isSaveSuccess) {
            logger.error("[AbstractPropertyCreditOrderCreator.doCreatePretenderOrder] failed ,save pretender order failed,pretenderAccount={}", pretenderAccount);
            throw new BizException(SYS_OPERATION_FAIL_CREATE);
        }
        return pretenderOrder;
    }


}
