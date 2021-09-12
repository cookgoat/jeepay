package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.service.mapper.PretenderAccountMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 伪装账号，用于调取上游支付接口 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
@Service
public class PretenderAccountService extends ServiceImpl<PretenderAccountMapper, PretenderAccount>  {

   public   PretenderAccount randomByBizType  (String bizType){
       return  this.getBaseMapper().randomByBizType(bizType);
    }



}
