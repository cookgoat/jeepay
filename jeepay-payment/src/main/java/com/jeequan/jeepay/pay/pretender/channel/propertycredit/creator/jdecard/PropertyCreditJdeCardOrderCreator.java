package com.jeequan.jeepay.pay.pretender.channel.propertycredit.creator.jdecard;

import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.pay.pretender.model.FacePrice;
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

    private static final List<FacePrice> jdECardFacePriceList = FacePriceConvert.batchConvertToFacePrice(GoodsWrapper.getJdECardGoodsType());

    @Override
    protected List<FacePrice> getAvailableFacePrice() {
       return jdECardFacePriceList;
    }

    @Override
    protected ProductTypeEnum getProductTypeEnum() {
        return ProductTypeEnum.JD_E_CARD;
    }

}
