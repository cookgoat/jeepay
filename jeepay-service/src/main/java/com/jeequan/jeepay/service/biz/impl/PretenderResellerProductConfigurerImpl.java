package com.jeequan.jeepay.service.biz.impl;

import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.entity.PretenderProduct;
import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;
import com.jeequan.jeepay.service.impl.PretenderProductService;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.service.impl.ResellerPretenderProductService;
import com.jeequan.jeepay.service.biz.PretenderResellerProductConfigurer;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Service
public class PretenderResellerProductConfigurerImpl implements PretenderResellerProductConfigurer {

  @Autowired
  private ResellerPretenderProductService resellerPretenderProductService;

  @Autowired
  private PretenderProductService pretenderProductService;

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void config(ResellerPretenderProduct resellerPretenderProduct, SysUser sysUser) {
    checkParam(resellerPretenderProduct);
    checkCredit(resellerPretenderProduct);
    isResellerHasProductAuth(resellerPretenderProduct);
    saveOrUpdate(resellerPretenderProduct, sysUser);
  }

  private void checkParam(ResellerPretenderProduct resellerPretenderProduct) {
    if (resellerPretenderProduct == null ||
        StringUtils.isEmpty(resellerPretenderProduct.getResellerNo()) ||
        StringUtils.isEmpty(resellerPretenderProduct.getProductType()) ||
        StringUtils.isEmpty(resellerPretenderProduct.getStatus())) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
    if(StringUtils.equalsIgnoreCase(resellerPretenderProduct.getStatus(),SwitchStatusEnum.ENABLE.getCode())){
      boolean isNullValue = resellerPretenderProduct.getFeeRate() == null ||
          resellerPretenderProduct.getFeeRate().compareTo(BigDecimal.ZERO) <= 0;
      if(isNullValue){
        throw new BizException(ApiCodeEnum.PARAMS_ERROR);
      }
    }
    validateProductType(resellerPretenderProduct.getProductType());
  }

  private void validateProductType(String productType) {
    int enableProductCount = pretenderProductService.count(
        PretenderProduct.gw().eq(PretenderProduct::getProductType, productType)
            .eq(PretenderProduct::getStatus,
                SwitchStatusEnum.ENABLE.getCode()));
    if (enableProductCount <= 0) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
  }

  private void saveOrUpdate(ResellerPretenderProduct resellerPretenderProduct,
      SysUser currentUser) {
    if (isResellerHasProductAuth(resellerPretenderProduct)) {
      //update
      resellerPretenderProduct.setUpdateUid(currentUser.getSysUserId());
      update(resellerPretenderProduct);
      return;
    }
    //add
    resellerPretenderProduct.setCreateUid(resellerPretenderProduct.getCreateUid());
    resellerPretenderProductService.save(resellerPretenderProduct);
  }


  private boolean isResellerHasProductAuth(
      ResellerPretenderProduct resellerPretenderProduct) {
    return resellerPretenderProductService
        .count(ResellerPretenderProduct.gw().eq(ResellerPretenderProduct::getProductType,
            resellerPretenderProduct.getProductType())
            .eq(ResellerPretenderProduct::getResellerNo,
                resellerPretenderProduct.getResellerNo())) > 0;
  }

  private void update(ResellerPretenderProduct resellerPretenderProduct) {
    LambdaUpdateWrapper<ResellerPretenderProduct> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper
        .set(ResellerPretenderProduct::getUpdateUid, resellerPretenderProduct.getUpdateUid());
    if (resellerPretenderProduct.getCreditAmount() != null) {
      updateWrapper.set(ResellerPretenderProduct::getCreditAmount,
          resellerPretenderProduct.getCreditAmount());
    }
    updateWrapper.set(ResellerPretenderProduct::getFeeRate, resellerPretenderProduct.getFeeRate());
    updateWrapper
        .set(ResellerPretenderProduct::getStatus, resellerPretenderProduct.getStatus());
    updateWrapper
        .eq(ResellerPretenderProduct::getResellerNo, resellerPretenderProduct.getResellerNo());
    updateWrapper
        .eq(ResellerPretenderProduct::getProductType, resellerPretenderProduct.getProductType());
    boolean isUpdateProductSuc=resellerPretenderProductService.update(updateWrapper);
    if(!isUpdateProductSuc){
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }
    //if close the product for the reseller,close all the reseller's order
    if(StringUtils.equalsIgnoreCase(resellerPretenderProduct.getStatus(),SwitchStatusEnum.ENABLE.getCode())){
      resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>().set(ResellerOrder::getOrderStatus,
          ResellerOrderStatusEnum.WAITING_PAY).eq(ResellerOrder::getOrderStatus,
          ResellerOrderStatusEnum.NULLIFY).eq(ResellerOrder::getResellerNo,resellerPretenderProduct.getResellerNo()));
    }else{
      resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>().set(ResellerOrder::getOrderStatus,
          ResellerOrderStatusEnum.NULLIFY).eq(ResellerOrder::getOrderStatus,
          ResellerOrderStatusEnum.WAITING_PAY).eq(ResellerOrder::getResellerNo,resellerPretenderProduct.getResellerNo()));
    }
  }

  private void checkCredit(ResellerPretenderProduct resellerPretenderProduct){
    if(resellerPretenderProduct.getRecoveriesAmount()==null){
      resellerPretenderProduct.setRecoveriesAmount(0L);
    }
    if(resellerPretenderProduct.getCreditAmount()!=null&&resellerPretenderProduct.getCreditAmount()>0){
      if(resellerPretenderProduct.getRecoveriesAmount()>resellerPretenderProduct.getCreditAmount()){
        throw new BizException(ApiCodeEnum.RESELLER_ACCOUNT_NOT_EXIST);
      }
    }
  }

}
