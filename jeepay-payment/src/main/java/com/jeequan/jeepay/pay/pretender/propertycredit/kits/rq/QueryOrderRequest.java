package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq;

import lombok.Data;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Data
public class QueryOrderRequest {

    private String merchantId = CS.RECHARGE_MERCHANT_ID;

    private String orderNo;

    private String cookie;

}
