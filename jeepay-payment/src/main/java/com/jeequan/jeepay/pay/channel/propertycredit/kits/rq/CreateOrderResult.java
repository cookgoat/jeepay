package com.jeequan.jeepay.pay.channel.propertycredit.kits.rq;

import lombok.Data;
import com.jeequan.jeepay.pay.channel.propertycredit.kits.rs.BaseResult;

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
