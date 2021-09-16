package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.service.mapper.ResellerOrderMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 核销上订单，提供可供上游支付 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
@Service
public class ResellerOrderService extends ServiceImpl<ResellerOrderMapper, ResellerOrder> {

    public   ResellerOrder randomByAmountAndProductType(Long amount ,String productType){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("amount",amount);
        paramMap.put("productType",productType);
        return  this.getBaseMapper().randomByAmountAndProductType(paramMap);
    }
}
