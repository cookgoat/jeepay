package com.jeequan.jeepay.core.constants;

/**
 * @author axl rose
 * @date 2021/9/13
 */
public enum PretenderOrderStatusEnum {
    PAYING("PAYING","支付中"),
    OVER_TIME("OVER_TIME","超时"),
    REQUEST_FAILED("REQUEST_FAILED","请求失败"),
    FINISH("FINISH","完成支付");

    private final String code;

    private final String msg;

    PretenderOrderStatusEnum(String code, String msg) {
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
