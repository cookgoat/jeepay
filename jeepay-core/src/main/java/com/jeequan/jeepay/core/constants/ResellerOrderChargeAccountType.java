package com.jeequan.jeepay.core.constants;

/**
 * @author axl rose
 * @date 2021/9/14
 */
public enum ResellerOrderChargeAccountType {
    MOBILE("MOBILE","手机号"),
    PLATFORM_ACCOUNT("PLATFORM_ACCOUNT","平台账号");

    private String code;

    private String msg;

    ResellerOrderChargeAccountType(String code, String msg) {
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
