package com.jeequan.jeepay.pay.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.BizTypeEnum;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.model.params.ProxyParams;
import com.jeequan.jeepay.pay.pretender.proxy.ProxyIpHunter;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.PropertyCreditUtil;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rs.QueryOrderResult;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.QueryOrderRequest;


@Service
public class ChannelOrderQueryImpl implements ChannelOrderQuery {

  @Autowired
  private ProxyIpHunter proxyIpHunter;

  @Autowired
  private PretenderAccountService pretenderAccountService;


  @Override
  public JSONObject queryByChannelOrderId(String channelOrderId) {
    ProxyParams proxyParams = proxyIpHunter.huntProxy();
    PropertyCreditUtil propertyCreditUtil = new PropertyCreditUtil(proxyParams);
    PretenderAccount pretenderAccount = findAccount();
    QueryOrderRequest queryOrderRequest = new QueryOrderRequest();
    queryOrderRequest.setOrderNo(channelOrderId);
    queryOrderRequest.setCookie(pretenderAccount.getCertificate());
    QueryOrderResult queryOrderResult = propertyCreditUtil.queryOrder(queryOrderRequest);
    JSONObject jsonObject = new JSONObject();
    if (queryOrderResult != null) {
      jsonObject.put("data", JSONObject.toJSONString(queryOrderResult));
    } else {
      jsonObject.put("data", "{}");
    }
    jsonObject.put("isSuccess", "true");
    return jsonObject;
  }

  private PretenderAccount findAccount() {
    PretenderAccount pretenderAccount = pretenderAccountService
        .randomByBizType(BizTypeEnum.PROPERTY_CREDIT.getCode());
    if (pretenderAccount == null) {
      throw new BizException(ApiCodeEnum.CUSTOM_FAIL);
    }
    return pretenderAccount;
  }

}
