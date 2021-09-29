package com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.model;

import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class Goods {
    @JSONField(name="face_price")
    private String facePrice;

    @JSONField(name="goods_name")
    private String goodsName;

    @JSONField(name="discount")
    private String discount;


    @JSONField(name="limit_price")
    private String limitPrice;

    @JSONField(name="is_discount")
    private String isDiscount;

    @JSONField(name="goods_id")
    private String goodsId;


}
