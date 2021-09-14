package com.jeequan.jeepay.pay.pretender.impl;

import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.pay.pretender.model.FacePrice;
import com.jeequan.jeepay.pay.pretender.model.convert.FacePriceConvert;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.model.GoodsWrapper;

import java.util.List;

/**
 * @author axl rose
 *
 */
public abstract class PropertyCreditCtripCreator extends AbstractPropertyCreditOrderCreator{


    @Override
    protected List<FacePrice> getAvailableFacePrice() {
        return FacePriceConvert.batchConvertToFacePrice(GoodsWrapper.getCtrpGnetGoodsType());
    }

    @Override
    protected ProductTypeEnum getProductTypeEnum() {
        return ProductTypeEnum.CTRIP;
    }

}
