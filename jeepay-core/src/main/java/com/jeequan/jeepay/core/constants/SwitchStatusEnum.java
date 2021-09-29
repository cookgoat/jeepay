package com.jeequan.jeepay.core.constants;

public enum SwitchStatusEnum {
  ENABLE("ENABLE","开启的"),
  DISABLE("DISABLE","关闭的");

  private final String code;

  private final String msg;

  SwitchStatusEnum(String code, String msg) {
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
