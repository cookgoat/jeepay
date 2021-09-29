package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.model.params.reseller.ResellerFundAccountParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.ResellerFundAccount;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.service.mapper.ResellerFundAccountMapper;

/**
 * <p>
 * 核销商资金账户表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-23
 */
@Service
public class ResellerFundAccountService extends ServiceImpl<ResellerFundAccountMapper, ResellerFundAccount> {

    public IPage<ResellerFundAccount> page(IPage iPage, ResellerFundAccountParams params){
        LambdaQueryWrapper<ResellerFundAccount> condition = ResellerFundAccount.gw();
        //参数处理
        packageParam(condition,params);
        return super.page(iPage,condition);
    }

    private void packageParam(LambdaQueryWrapper<ResellerFundAccount> condition, ResellerFundAccountParams params){
        if(StringUtils.isNotEmpty(params.getResellerNo())){
            condition.eq(ResellerFundAccount::getResellerNo,params.getResellerNo());
        }
        if(params.getStartDate() != null){
            condition.ge(ResellerFundAccount::getGmtCreate,params.getStartDate());
        }
        if(params.getEndDate() != null){
            condition.le(ResellerFundAccount::getGmtCreate,params.getEndDate());
        }
        if(params.getAmountStart() != null){
            condition.ge(ResellerFundAccount::getRecoveriesAllAmount,params.getAmountStart());
        }
        if(params.getAmountEnd() != null){
            condition.le(ResellerFundAccount::getRecoveriesAllAmount,params.getAmountEnd());
        }
    }

}