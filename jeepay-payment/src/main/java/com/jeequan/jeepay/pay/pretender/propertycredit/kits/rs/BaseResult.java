package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;

/**
 * @author axl rose
 * @date 2021/9/8
 */
@Data
public class BaseResult {

    @JSONField
    private String resultCode;

    @JSONField
    private String resultMsg;

    public boolean isSuccess(){
        return StringUtils.equalsAnyIgnoreCase(this.resultCode, CS.PROPERTY_CREDIT_API_RESULT_CODE.SUCCESS_CODE);
    }
}
