package com.jeequan.jeepay.pay.channel.propertycredit.kits.rq;

import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.pay.channel.propertycredit.kits.CS;

/**
 * @author axl rose
 * @date 2021/9/9
 */
@Data
public class BasePayRequest {
    private String payAmount;
    private String rechargeAccount;
    private String rechargeType;
    private String rechargeAmount;
    private String goodsName;
    private String orderNo;
    private String merchantId = CS.RECHARGE_MERCHANT_ID;
    private String isPaying;
    @JSONField(name="Cookie")
    private String cookie;
}
