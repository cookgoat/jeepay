package com.jeequan.jeepay.pay.channel.propertycredit.kits.model;

import lombok.Data;
import java.util.List;
import com.alibaba.fastjson.annotation.JSONField;


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
   List<Goods> customValue;

   @JSONField(name="face_price_list")
   List<Goods> facePriceList;

}
