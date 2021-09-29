package com.jeequan.jeepay.pay.pretender.match;

import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.pay.pretender.OrderMatchUrlGenerator;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("generateMatchUrl")
public class GenerateMatchUrlPretenderOrderCreator implements OrderMatchUrlGenerator {

    @Autowired
    private ISysConfigService sysConfigService;

    @Override
    public String generate(String orderNo) {
      return   sysConfigService.getDBApplicationConfig().genMatchOrderUrl(orderNo);
    }

}
