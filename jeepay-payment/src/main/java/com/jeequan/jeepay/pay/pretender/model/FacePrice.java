package com.jeequan.jeepay.pay.pretender.model;

import lombok.Data;

/**
 * @author axl rose
 * @date 2021/9/12
 */
@Data
public class FacePrice {

    private Long facePrice;

    private Long discount;

    private Long limitPrice;

    private boolean isDiscount;

    private boolean isCustom;

    private String goodsId;

    private String productCode;

    private String goodType;

}
