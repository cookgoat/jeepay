package com.jeequan.jeepay.pay.pretender.propertycredit.kits.model;

import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class GoodsWrapper {
    /**
     * jd e card
     */
    private GoodsType jdECard;
    /**
     * ctrp gnet card
     */
    private GoodsType ctrpGnet;


    public static GoodsType getJdECardGoodsType(){
        return getGoodsTypeByBrandId(CS.PROPERTY_CREDIT_BRAND_IDS.JD);
    }

    public static GoodsType getCtrpGnetGoodsType(){
        return getGoodsTypeByBrandId(CS.PROPERTY_CREDIT_BRAND_IDS.CTRP);
    }

    private static  GoodsType  getGoodsTypeByBrandId(String brandId){
        GoodsList goodsList = GoodsList.of();
        Optional<Brand>  brandOpt =  goodsList.getBrandList().stream()
                .filter(brand -> StringUtils.equalsIgnoreCase(brand.getBrandId(), brandId)).findAny();
        if(!brandOpt.isPresent()||(brandOpt.get().getGoodTypeList()==null)){
            throw new IllegalArgumentException("jd goods type is not exist");
        }
        Optional<GoodsType> goodsTypeOpt = brandOpt.get().getGoodTypeList().stream()
                .filter(goodsType -> StringUtils.equalsIgnoreCase(goodsType.getChzTypeId(),brandId+CS.GOODS_ID_BUILD_STR)).findAny();
        if(!goodsTypeOpt.isPresent()){
            throw new IllegalArgumentException("jd goods type is not exist");
        }
        return goodsTypeOpt.get();
    }

}
