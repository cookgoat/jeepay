package com.jeequan.jeepay.pay.pretender.propertycredit.kits.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;


/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class GoodsType {
   @JSONField(name="chz_type_name")
   private String typeName;

   @JSONField(name="chz_type_id")
   private String chzTypeId;

   @JSONField(name="custom_value")
  private Goods customValue;

   @JSONField(name="face_price_list")
   private List<Goods> facePriceList;

}
