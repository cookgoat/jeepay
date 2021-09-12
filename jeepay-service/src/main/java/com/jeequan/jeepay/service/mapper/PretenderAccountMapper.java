package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 伪装账号，用于调取上游支付接口 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
public interface PretenderAccountMapper extends BaseMapper<PretenderAccount> {

    PretenderAccount randomByBizType  (@Param("bizType") String bizType);

}
