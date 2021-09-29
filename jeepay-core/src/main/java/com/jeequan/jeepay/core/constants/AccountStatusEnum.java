package com.jeequan.jeepay.core.constants;

import org.apache.commons.lang3.StringUtils;

public enum AccountStatusEnum {
  ENABLE("ENABLE",Byte.valueOf("1"),"可用的"),
  DISABLE("DISABLE",Byte.valueOf("0"),"不可用的");

  private String code;
  private byte codeByte;

  private String msg;

  AccountStatusEnum(String code,byte codeByte, String msg) {
    this.code = code;
    this.codeByte = codeByte;
    this.msg = msg;
  }

  public String getCode(){
    return this.code;
  }

  public byte getCodeByte(){
    return this.codeByte;
  }


  public String getMsg() {
    return this.msg;
  }

  public static boolean isRightStatus(String code){
    for(AccountStatusEnum accountStatusEnum: AccountStatusEnum.values()){
      if(StringUtils.equalsIgnoreCase(accountStatusEnum.getCode(),code)){
        return true;
      }
    }
    return false;
  }
}
