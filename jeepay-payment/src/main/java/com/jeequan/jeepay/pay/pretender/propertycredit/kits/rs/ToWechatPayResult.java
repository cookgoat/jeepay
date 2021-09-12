package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs;

import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author axl rose
 * @date 2021/9/10
 */
@Data
public class ToWechatPayResult extends BaseResult{

    @JSONField
    private String data;

}
