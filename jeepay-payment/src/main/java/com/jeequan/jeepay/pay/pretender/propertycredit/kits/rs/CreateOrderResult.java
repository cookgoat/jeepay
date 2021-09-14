package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs;

import lombok.Data;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class CreateOrderResult extends BaseResult {
 private String orderNo;
 private String payAmount;
 private String goodsName;
}
