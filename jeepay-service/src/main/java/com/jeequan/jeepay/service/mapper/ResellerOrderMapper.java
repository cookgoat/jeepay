package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * <p>
 * 核销上订单，提供可供上游支付 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
public interface ResellerOrderMapper extends BaseMapper<ResellerOrder> {

    ResellerOrder randomByAmountAndProductType  (Map<String,Object> paramMap);


}
