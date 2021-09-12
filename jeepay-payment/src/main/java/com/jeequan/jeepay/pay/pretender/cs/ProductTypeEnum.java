package com.jeequan.jeepay.pay.pretender.cs;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public enum ProductTypeEnum {

    JD_E_CARD("JD_E_CARD","京东E卡");

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


}
