package com.jeequan.jeepay.pay.pretender.channel.propertycredit.creator.jdecard;

import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.pay.pretender.model.ProductFacePrice;
import com.jeequan.jeepay.pay.pretender.model.convert.FacePriceConvert;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.creator.AbstractPropertyCreditOrderCreator;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.model.GoodsWrapper;

import java.util.List;


/**
 * @author axl rose
 * @date 2021/9/13
 */
abstract
public class PropertyCreditJdeCardOrderCreator extends AbstractPropertyCreditOrderCreator {

    private static final List<ProductFacePrice> jdECardFacePriceList = FacePriceConvert.batchConvertToFacePrice(GoodsWrapper.getJdECardGoodsType());

    @Override
    protected List<ProductFacePrice> getAvailableFacePrice() {
       return jdECardFacePriceList;
    }

    @Override
    protected ProductTypeEnum getProductTypeEnum() {
        return ProductTypeEnum.JD_E_CARD;
    }

}
