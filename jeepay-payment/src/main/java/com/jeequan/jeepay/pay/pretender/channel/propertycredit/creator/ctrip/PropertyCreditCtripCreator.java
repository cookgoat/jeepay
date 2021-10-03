package com.jeequan.jeepay.pay.pretender.channel.propertycredit.creator.ctrip;

import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.pay.pretender.model.ProductFacePrice;
import com.jeequan.jeepay.pay.pretender.model.convert.FacePriceConvert;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.creator.AbstractPropertyCreditOrderCreator;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.model.GoodsWrapper;

import java.util.List;

/**
 * @author axl rose
 */
public abstract class PropertyCreditCtripCreator extends AbstractPropertyCreditOrderCreator {

  private static final List<ProductFacePrice> ctrpFacePriceList = FacePriceConvert.batchConvertToFacePrice(
      GoodsWrapper.getCtrpGnetGoodsType());

  @Override
  protected List<ProductFacePrice> getAvailableFacePrice() {
    return ctrpFacePriceList;
  }

  @Override
  protected ProductTypeEnum getProductTypeEnum() {
    return ProductTypeEnum.CTRIP;
  }

}
