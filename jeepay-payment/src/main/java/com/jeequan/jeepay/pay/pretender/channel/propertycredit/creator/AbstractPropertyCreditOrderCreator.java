package com.jeequan.jeepay.pay.pretender.channel.propertycredit.creator;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.GET_ALIPAY_FAILED;
import static com.jeequan.jeepay.core.constants.ApiCodeEnum.PRETENDER_ACCOUNT_IS_NOT_LOGIN;
import static com.jeequan.jeepay.core.constants.ApiCodeEnum.TO_ALIPAY_FAILED;
import com.jeequan.jeepay.core.constants.BizTypeEnum;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.pretender.AbstractPretenderCreator;
import com.jeequan.jeepay.pay.pretender.PretenderOrderCreator;
import com.jeequan.jeepay.core.constants.PretenderOrderStatusEnum;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.GetUserGuide;
import com.jeequan.jeepay.pay.pretender.model.ProductFacePrice;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.AlipayHelper;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.CS;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.PropertyCreditUtil;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.StringFinder;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.BasePayRequest;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.CreateOrderRequest;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.GetRechargeAccountLogRequest;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rs.BaseResult;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rs.CreateOrderResult;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rs.ToWechatPayResult;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  protected PretenderOrder doCreateOrder(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount, ProductFacePrice facePrice) {
    PropertyCreditUtil propertyCreditUtil = new PropertyCreditUtil(getProxy());
    taskExec.submit(() -> doCommonSimulateAction(pretenderAccount, facePrice, propertyCreditUtil));
    return doCreatePretenderOrder(resellerOrder, pretenderAccount, facePrice, propertyCreditUtil);
  }

  private void doCommonSimulateAction(PretenderAccount pretenderAccount, ProductFacePrice facePrice,
      PropertyCreditUtil propertyCreditUtil) {
    //request recharge page,just for simulate
    propertyCreditUtil.requestRechargePage();
    //check the pretender account whether login
    checkPretenderAccountWhetherLogin(pretenderAccount, propertyCreditUtil);
    //get the recharge account log,just for simulate
    GetRechargeAccountLogRequest getRechargeAccountLogRequest = new GetRechargeAccountLogRequest();
    getRechargeAccountLogRequest.setCookie(pretenderAccount.getCertificate());
    getRechargeAccountLogRequest.setRechargeProduct(facePrice.getProductCode());
    propertyCreditUtil.getRechargeAccountLog(getRechargeAccountLogRequest);
    GetUserGuide getUserGuide = new GetUserGuide();
    getUserGuide.setRechargeProduct(facePrice.getProductCode());
    getUserGuide.setType(facePrice.getGoodType());
    getUserGuide.setCookie(pretenderAccount.getCertificate());
    propertyCreditUtil.getUserGuide(getUserGuide);
  }


  private PretenderOrder doCreatePretenderOrder(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount, ProductFacePrice facePrice,
      PropertyCreditUtil propertyCreditUtil) {
    //create order request
    CreateOrderRequest createOrderRequest = buildCreateOrderRequest(resellerOrder, pretenderAccount,
        facePrice);
    CreateOrderResult createOrderResult = propertyCreditUtil.createOrder(createOrderRequest);
    if (!createOrderResult.isSuccess()) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.doCreatePretenderOrder] failed,pretenderAccount={},createOrderRequest={},createOrderResult={}",
          pretenderAccount,
          createOrderRequest, createOrderResult);
      throw new BizException(PRETENDER_ACCOUNT_IS_NOT_LOGIN);
    }
    BasePayRequest basePayRequest = buildBasePayRequest(createOrderRequest, createOrderResult);
    taskExec.submit(() -> propertyCreditUtil.goPay(basePayRequest));
    String payUrl;
    if (StringUtils.equalsIgnoreCase(getPayWay(),
        com.jeequan.jeepay.core.constants.CS.PAY_WAY_CODE.ALI_WAP)) {
      payUrl = getAlipayUrl(basePayRequest, propertyCreditUtil);
    } else {
      payUrl = getWechatUrl(basePayRequest, propertyCreditUtil);
    }
    return buildPretenderOrder(resellerOrder, pretenderAccount, createOrderResult, payUrl);
  }


  private CreateOrderRequest buildCreateOrderRequest(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount, ProductFacePrice facePrice) {
    CreateOrderRequest createOrderRequest = new CreateOrderRequest();
    createOrderRequest.setCookie(pretenderAccount.getCertificate());
    createOrderRequest.setGoodsId(facePrice.getGoodsId());
    createOrderRequest.setRechargeProduct(facePrice.getProductCode());
    createOrderRequest.setRechargeAccount(resellerOrder.getChargeAccount());
    createOrderRequest.setRechargeType(facePrice.getGoodType());
    Long discount = AmountUtil.calPercentageFee(resellerOrder.getAmount(),
        BigDecimal.valueOf(facePrice.getDiscount()));
    Long amount = resellerOrder.getAmount() + discount;
    String amountDollar = AmountUtil.convertCent2Dollar(amount, 0);
    createOrderRequest.setRechargeAmount(amountDollar);
    return createOrderRequest;
  }


  private BasePayRequest buildBasePayRequest(CreateOrderRequest createOrderRequest,
      CreateOrderResult createOrderResult) {
    BasePayRequest basePayRequest = new BasePayRequest();
    basePayRequest.setCookie(createOrderRequest.getCookie());
    basePayRequest.setRechargeAccount(createOrderRequest.getRechargeAccount());
    basePayRequest.setRechargeType(createOrderRequest.getRechargeType());
    basePayRequest.setIsPaying(CS.PROPERTY_CREDIT_API_RESULT_CODE.IS_PAYING);
    basePayRequest.setPayAmount(createOrderRequest.getRechargeAmount());
    basePayRequest.setOrderNo(createOrderResult.getOrderNo());
    basePayRequest.setRechargeAmount(createOrderRequest.getRechargeAmount());
    basePayRequest.setGoodsName(
        getProductTypeEnum().getMsg() + createOrderRequest.getRechargeAmount() + "元");
    return basePayRequest;
  }


  private void checkPretenderAccountWhetherLogin(PretenderAccount pretenderAccount,
      PropertyCreditUtil propertyCreditUtil) {
    BaseResult baseResult = propertyCreditUtil.isLogin(pretenderAccount.getCertificate());
    if (!baseResult.isSuccess()) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.checkPretenderAccountWhetherLogin] is not login,pretenderAccount={}",
          pretenderAccount);
      throw new BizException(PRETENDER_ACCOUNT_IS_NOT_LOGIN);
    }
  }

  private String getAlipayUrl(BasePayRequest basePayRequest,
      PropertyCreditUtil propertyCreditUtil) {
    String alipayJsonData = propertyCreditUtil.toAlipay(basePayRequest);
    if (StringUtils.isBlank(alipayJsonData)) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.getAlipayUrl] to alipay failed,basePayRequest={},alipayJsonData={}",
          basePayRequest, alipayJsonData);
      throw new BizException(TO_ALIPAY_FAILED);
    }
    Map<String, String> alipayParams = AlipayHelper.getAlipayUrlFromAction(alipayJsonData);
    String payUrl = propertyCreditUtil.postAlipay(alipayParams);
    if (StringUtils.isBlank(payUrl)) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.getAlipayUrl] postAlipay failed,basePayRequest={},payUrl={}",
          basePayRequest, payUrl);
      throw new BizException(GET_ALIPAY_FAILED);
    }
    return payUrl;
  }


  private String getWechatUrl(BasePayRequest basePayRequest,
      PropertyCreditUtil propertyCreditUtil) {
    ToWechatPayResult toWechatPayResult = propertyCreditUtil.toWechatPay(basePayRequest);
    if (!toWechatPayResult.isSuccess()) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.getAlipayUrl] to alipay failed,basePayRequest={},toWechatPayResult={}",
          basePayRequest, toWechatPayResult);
      throw new BizException(TO_ALIPAY_FAILED);
    }
    String pageString = propertyCreditUtil.postWechatPay(toWechatPayResult.getData());
    if (StringUtils.isBlank(pageString)) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.getAlipayUrl] postAlipay failed,basePayRequest={},pageString={}",
          basePayRequest, pageString);
      throw new BizException(GET_ALIPAY_FAILED);
    }
    String payUrl = StringFinder.findWeChatDeeplinkFromString(pageString);
    if (StringUtils.isBlank(payUrl)) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.getAlipayUrl] postAlipay failed,payUrl is blank,basePayRequest={},pageString={},payUrl={}",
          basePayRequest, pageString, payUrl);
      throw new BizException(GET_ALIPAY_FAILED);
    }
    return payUrl;
  }

  private PretenderOrder buildPretenderOrder(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount, CreateOrderResult createOrderResult, String payUrl) {
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
    pretenderOrder.setProductType(getProductTypeEnum().getCode());
    return pretenderOrder;
  }


}
