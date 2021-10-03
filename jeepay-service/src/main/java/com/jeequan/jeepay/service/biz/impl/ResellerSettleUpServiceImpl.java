package com.jeequan.jeepay.service.biz.impl;

import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.entity.ResellerFundLine;
import com.jeequan.jeepay.core.constants.FundLineBizType;
import com.jeequan.jeepay.core.entity.ResellerFundAccount;
import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.biz.ResellerSettleUpService;
import com.jeequan.jeepay.service.impl.ResellerFundLineService;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.service.impl.ResellerFundAccountService;
import com.jeequan.jeepay.service.impl.ResellerPretenderProductService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Service
public class ResellerSettleUpServiceImpl implements ResellerSettleUpService {

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Autowired
  private ResellerFundAccountService resellerFundAccountService;


  @Autowired
  private ResellerFundLineService resellerFundLineService;

  @Autowired
  private ResellerPretenderProductService resellerPretenderProductService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void settleUp(String resellerNo, String productType) {
    ResellerFundAccount resellerFundAccount = resellerFundAccountService
        .getOne(ResellerFundAccount.gw().eq(ResellerFundAccount::getResellerNo, resellerNo));
    ResellerPretenderProduct resellerPretenderProduct = resellerPretenderProductService.getOne(
        ResellerPretenderProduct.gw().eq(ResellerPretenderProduct::getProductType, productType));
    if (resellerPretenderProduct == null) {
      return;
    }
    if (resellerPretenderProduct.getRecoveriesAmount() <= 0) {
      return;
    }
    resellerPretenderProductService.update(new LambdaUpdateWrapper<ResellerPretenderProduct>()
        .set(ResellerPretenderProduct::getRecoveriesAmount, 0L)
        .set(ResellerPretenderProduct::getStatus,
            SwitchStatusEnum.ENABLE.getCode())
        .eq(ResellerPretenderProduct::getId, resellerPretenderProduct.getId()));

    resellerFundAccountService.update(new LambdaUpdateWrapper<ResellerFundAccount>()
        .set(ResellerFundAccount::getRecoveriesAllAmount,
            resellerFundAccount.getRecoveriesAllAmount() - resellerPretenderProduct
                .getRecoveriesAmount()).eq(ResellerFundAccount::getResellerNo, resellerNo));
    ResellerFundLine resellerFundLine = buildResellerFundLine(resellerPretenderProduct,
        resellerFundAccount);
    resellerFundLineService.save(resellerFundLine);
    resellerOrderService
        .update(new LambdaUpdateWrapper<ResellerOrder>().set(ResellerOrder::getOrderStatus,
            ResellerOrderStatusEnum.WAITING_MATCH).eq(ResellerOrder::getResellerNo, resellerNo)
            .eq(ResellerOrder::getProductType, productType).eq(ResellerOrder::getOrderStatus,
                ResellerOrderStatusEnum.NULLIFY));
  }

  private ResellerFundLine buildResellerFundLine(ResellerPretenderProduct resellerPretenderProduct,
      ResellerFundAccount resellerFundAccount) {
    ResellerFundLine resellerFundLine = new ResellerFundLine();
    resellerFundLine.setBizType(FundLineBizType.SETTLE_UP.getCode());
    resellerFundLine.setChangeRecoveriesAmount(-resellerPretenderProduct.getRecoveriesAmount());
    resellerFundLine.setBeforeRecoveriesAmount(resellerFundAccount.getRecoveriesAllAmount());
    resellerFundLine.setAfterRecoveriesAmount(
        resellerFundAccount.getRecoveriesAllAmount() - resellerPretenderProduct
            .getRecoveriesAmount());
    resellerFundLine.setResellerShareAmount(resellerFundAccount.getShareAllAmount());
    resellerFundLine.setResellerNo(resellerFundAccount.getResellerNo());
    resellerFundLine.setOrderAmount(0L);
    resellerFundLine.setProductType(resellerPretenderProduct.getProductType());
    resellerFundLine.setProductOrderNo("");
    return resellerFundLine;
  }


}
