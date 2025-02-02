package com.jeequan.jeepay.pay.pretender.model.convert;

import com.alibaba.druid.util.StringUtils;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.pretender.model.ProductFacePrice;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.model.Goods;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.model.GoodsType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.CS.IS_DISCOUNT;

/**
 * @author axl rose
 * @date 2021/9/13
 */
public class FacePriceConvert {


    public static List<ProductFacePrice> batchConvertToFacePrice(GoodsType goodsType) {
        List<ProductFacePrice> facePriceList = new ArrayList<>(5);
        for (Goods goods : goodsType.getFacePriceList()) {
            ProductFacePrice facePrice = FacePriceConvert.convertToFacePrice(goodsType, goods);
            facePriceList.add(facePrice);
        }
        if (goodsType.getCustomValue() != null) {
            ProductFacePrice facePrice = FacePriceConvert.convertToFacePrice(goodsType, goodsType.getCustomValue());
            facePrice.setCustom(true);
            facePriceList.add(facePrice);
        }
        return facePriceList;
    }

    public static ProductFacePrice convertToFacePrice(GoodsType goodsType, Goods goods) {
        ProductFacePrice facePrice = new ProductFacePrice();
        facePrice.setProductCode(goodsType.getBrandId());
        facePrice.setFacePrice(Long.valueOf(AmountUtil.convertDollar2Cent(goods.getFacePrice())));

        facePrice.setLimitPrice(Long.valueOf(AmountUtil.convertDollar2Cent(goods.getLimitPrice())));
        facePrice.setCustom(false);
        facePrice.setNeedDiscount(StringUtils.equalsIgnoreCase(goods.getIsDiscount(), IS_DISCOUNT));
        facePrice.setDiscount(BigDecimal.valueOf(Double.valueOf(goods.getDiscount()))
                .divide(new BigDecimal(100), 6, RoundingMode.HALF_UP).longValue());
        facePrice.setGoodType(goodsType.getChzTypeId());
        facePrice.setGoodsId(goods.getGoodsId());
        return facePrice;
    }

}
