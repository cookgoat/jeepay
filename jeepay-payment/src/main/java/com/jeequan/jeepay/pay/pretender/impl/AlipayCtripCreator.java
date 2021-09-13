package com.jeequan.jeepay.pay.pretender.impl;

import com.jeequan.jeepay.core.constants.CS;
import org.springframework.stereotype.Service;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("alipayCtripCreator")
public class AlipayCtripCreator extends PropertyCreditCtripCreator{
    @Override
    protected String getPayWay() {
        return CS.PAY_WAY_CODE.WX_H5;
    }
}
