package com.jeequan.jeepay.core.constants;

public enum FundLineBizType {
  SHARE("SHARE","分红"),
  SETTLE_UP("SETTLE_UP","清账");

  private final String code;

  private final String msg;

  FundLineBizType(String code, String msg) {
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
