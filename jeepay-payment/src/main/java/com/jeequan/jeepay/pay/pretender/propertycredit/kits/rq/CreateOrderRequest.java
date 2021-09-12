package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq;

import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class CreateOrderRequest {
    private String rechargeProduct;
    private String rechargeAccount;
    private String rechargeType;
    private String rechargeAmount;
    private String goodsId;
    @JSONField(name="Cookie")
    private String cookie;
    private String merchantId = CS.RECHARGE_MERCHANT_ID;
}
