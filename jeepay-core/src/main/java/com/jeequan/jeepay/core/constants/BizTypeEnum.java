package com.jeequan.jeepay.core.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public enum BizTypeEnum {

    PROPERTY_CREDIT("PROPERTY_CREDIT","资和信业务");

    private String code;

    private String msg;

    BizTypeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode(){
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public static boolean isRightBizType(String code){
        for(BizTypeEnum bizTypeEnum: BizTypeEnum.values()){
            if(StringUtils.equalsIgnoreCase(bizTypeEnum.getCode(),code)){
                return true;
            }
        }
        return false;
    }

}
