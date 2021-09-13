package com.jeequan.jeepay.pay.pretender.impl;

import com.jeequan.jeepay.pay.pretender.cs.ProductTypeEnum;
import com.jeequan.jeepay.pay.pretender.model.FacePrice;
import com.jeequan.jeepay.pay.pretender.model.convert.FacePriceConvert;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.model.GoodsWrapper;

import java.util.List;


/**
 * @author axl rose
 * @date 2021/9/13
 */
abstract
public class PropertyCreditJdeCardOrderCreator extends AbstractPropertyCreditOrderCreator{

    @Override
    protected List<FacePrice> getAvailableFacePrice() {
       return FacePriceConvert.batchConvertToFacePrice(GoodsWrapper.getJdECardGoodsType());
    }

    @Override
    protected ProductTypeEnum getProductTypeEnum() {
        return ProductTypeEnum.JD_E_CARD;
    }

}
