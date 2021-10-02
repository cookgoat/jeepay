package com.jeequan.jeepay.core.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public enum ProductTypeEnum {

    JD_E_CARD("JD_E_CARD","京东E卡"),
    CTRIP("CTRIP","携程任我行");

    private String code;

    private String msg;

    ProductTypeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode(){
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public static boolean isRightProductType(String code){
       for(ProductTypeEnum productTypeEnum: ProductTypeEnum.values()){
          if(StringUtils.equalsIgnoreCase(productTypeEnum.getCode(),code)){
              return true;
          }
       }
        return false;
    }

  public static ProductTypeEnum getType(String code){
    for(ProductTypeEnum productTypeEnum: ProductTypeEnum.values()){
      if(StringUtils.equalsIgnoreCase(productTypeEnum.getCode(),code)){
        return productTypeEnum;
      }
    }
    return null;
  }

}
