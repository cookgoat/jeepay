package com.jeequan.jeepay.pay.pretender.cs;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public enum PretenderAccountStatusEnum {

    AVAILABLE("AVAILABLE","可用的"),
    UNAVAILABLE("UNAVAILABLE","不可用的");

    private final String code;

    private final String msg;

    PretenderAccountStatusEnum(String code, String msg) {
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
