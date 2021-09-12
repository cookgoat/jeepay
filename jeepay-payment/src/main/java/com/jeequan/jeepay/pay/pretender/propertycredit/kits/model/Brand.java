package com.jeequan.jeepay.pay.pretender.propertycredit.kits.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class Brand {
    @JSONField(name="account_type")
    private String accountType;


    @JSONField(name="brand_name")
    private String brandName;


    @JSONField(name="brand_id")
    private String brandId;

   @JSONField(name="chz_type_list")
    private List<GoodsType> goodTypeList;

}
