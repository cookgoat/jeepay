package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq;

import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class GetRechargeAccountLogRequest {
    @JSONField
    private String merchantId = CS.RECHARGE_MERCHANT_ID;
    @JSONField
    private String rechargeProduct;
    private String cookie;
}
