package com.jeequan.jeepay.pay.pretender.model.convert;

import com.alibaba.druid.util.StringUtils;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.pay.pretender.model.FacePrice;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.model.Goods;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.model.GoodsType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS.IS_DISCOUNT;

/**
 * @author axl rose
 * @date 2021/9/13
 */
public class FacePriceConvert {


    public static List<FacePrice> batchConvertToFacePrice(GoodsType goodsType) {
        List<FacePrice> facePriceList = new ArrayList<>(5);
        for (Goods goods : goodsType.getFacePriceList()) {
            FacePrice facePrice = FacePriceConvert.convertToFacePrice(goodsType, goods);
            facePriceList.add(facePrice);
        }
        if (goodsType.getCustomValue() != null) {
            FacePrice facePrice = FacePriceConvert.convertToFacePrice(goodsType, goodsType.getCustomValue());
            facePrice.setCustom(true);
            facePriceList.add(facePrice);
        }
        return facePriceList;
    }

    public static FacePrice convertToFacePrice(GoodsType goodsType, Goods goods) {
        FacePrice facePrice = new FacePrice();
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
