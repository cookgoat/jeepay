package com.jeequan.jeepay.pay.pretender.cs;

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


}
