package com.jeequan.jeepay.core.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public enum ResellerOrderStatusEnum {

    WAITING_PAY("WAITING_PAY","等待支付"),

    PAYING("PAYING","支付中"),

    MATCHING("MATCHING","匹配中"),

    NULLIFY("NULLIFY","已经作废"),

    FINISH("FINISH","订单完成");

    private String code;

    private String msg;

    ResellerOrderStatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode(){
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }


    public static ResellerOrderStatusEnum getType(String code){
        for(ResellerOrderStatusEnum productTypeEnum: ResellerOrderStatusEnum.values()){
            if(StringUtils.equalsIgnoreCase(productTypeEnum.getCode(),code)){
                return productTypeEnum;
            }
        }
        return null;
    }

}
