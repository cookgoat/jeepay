package com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq;

import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.CS;
import lombok.Data;

@Data
public class GetUserGuide {
  @JSONField
  private String merchantId = CS.RECHARGE_MERCHANT_ID;
  private String rechargeProduct;
  private String type;
  private String cookie;
}
