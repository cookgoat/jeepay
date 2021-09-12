package com.jeequan.jeepay.pay.pretender.cs;

/**
 * @author axl rose
 * @date 2021/9/12
 */
public enum ResellerOrderStatusEnum {

    WAITING_PAY("WAITING_PAY","等待支付"),

    PAYING("PAYING","支付中"),

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

}
