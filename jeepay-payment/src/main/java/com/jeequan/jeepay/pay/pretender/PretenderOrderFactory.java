package com.jeequan.jeepay.pay.pretender;

import com.jeequan.jeepay.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import static com.jeequan.jeepay.core.constants.ApiCodeEnum.SYSTEM_ERROR;


/**
 * @author axl rose
 * @date 2021/9/13
 */
@Service("pretenderOrderCreatorFactory")
public class PretenderOrderFactory {

    @Autowired private Map<String, PretenderOrderCreator> pretenderOrderCreatorMap;

    public PretenderOrderCreator getInstance(String beanName) {
        if (pretenderOrderCreatorMap == null || pretenderOrderCreatorMap.size() <= 0) {
            throw new BizException(SYSTEM_ERROR);
        }
        PretenderOrderCreator pretenderOrderCreator = pretenderOrderCreatorMap.get(beanName);
        if (pretenderOrderCreator == null) {
            throw new BizException(SYSTEM_ERROR);
        }
        return pretenderOrderCreator;
    }

}
