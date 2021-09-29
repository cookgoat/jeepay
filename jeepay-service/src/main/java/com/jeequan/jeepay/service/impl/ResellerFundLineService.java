package com.jeequan.jeepay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.model.params.reseller.ResellerFundLineParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.ResellerFundLine;
import com.jeequan.jeepay.service.mapper.ResellerFundLineMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <p>
 * 核销商资金流水 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-23
 */
@Service
public class ResellerFundLineService extends ServiceImpl<ResellerFundLineMapper, ResellerFundLine> {

    public IPage<ResellerFundLine> page(IPage iPage, ResellerFundLineParams params){
        LambdaQueryWrapper<ResellerFundLine> condition = ResellerFundLine.gw();
        //参数处理
        packageParam(condition,params);
        return super.page(iPage,condition);
    }

    private void packageParam(LambdaQueryWrapper<ResellerFundLine> wrapper, ResellerFundLineParams params){
        if(StringUtils.isNotEmpty(params.getProductType())){
            wrapper.eq(ResellerFundLine::getProductType,params.getProductType());
        }
        if(StringUtils.isNotEmpty(params.getProductOrderNo())){
            wrapper.like(ResellerFundLine::getProductOrderNo,params.getProductOrderNo());
        }
        if(StringUtils.isNotEmpty(params.getResellerNo())){
            wrapper.eq(ResellerFundLine::getResellerNo,params.getResellerNo());
        }
        if(params.getAmountStart() != null){
            wrapper.ge(ResellerFundLine::getOrderAmount,params.getAmountStart());
        }
        if(params.getAmountEnd() != null){
            wrapper.le(ResellerFundLine::getOrderAmount,params.getAmountEnd());
        }
        if(params.getStartDate() != null){
            wrapper.ge(ResellerFundLine::getGmtCreate,params.getStartDate());
        }
        if(params.getEndDate() != null){
            wrapper.le(ResellerFundLine::getGmtCreate,params.getEndDate());
        }

    }

}
