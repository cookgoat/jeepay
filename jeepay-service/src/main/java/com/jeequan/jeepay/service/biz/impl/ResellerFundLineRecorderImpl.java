package com.jeequan.jeepay.service.biz.impl;

import com.jeequan.jeepay.core.constants.FundLineBizType;
import lombok.Data;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.entity.ResellerFundLine;
import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import com.jeequan.jeepay.core.entity.ResellerFundAccount;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;
import com.jeequan.jeepay.service.impl.ResellerFundLineService;
import com.jeequan.jeepay.service.biz.ResellerFundLineRecorder;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.service.impl.ResellerFundAccountService;
import com.jeequan.jeepay.core.entity.ResellerFundAccountSnapshot;
import com.jeequan.jeepay.core.constants.PretenderOrderStatusEnum;
import com.jeequan.jeepay.service.impl.ResellerPretenderProductService;
import com.jeequan.jeepay.service.impl.ResellerFundAccountSnapshotService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Service
public class ResellerFundLineRecorderImpl implements ResellerFundLineRecorder {

  @Autowired
  private ResellerFundLineService resellerFundLineService;

  @Autowired
  private ResellerFundAccountService resellerFundAccountService;

  @Autowired
  private ResellerPretenderProductService resellerPretenderProductService;

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Autowired
  private ResellerFundAccountSnapshotService resellerFundAccountSnapshotService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void record(PretenderOrder pretenderOrder) {
    checkParam(pretenderOrder);
    ResellerOrder resellerOrder = queryResellerOrder(pretenderOrder.getMatchResellerOrderNo());
    ResellerFundAccount resellerFundAccount = queryResellerAccount(resellerOrder.getResellerNo());
    ResellerPretenderProduct resellerPretenderProduct = queryResellerPretenderProduct(
        pretenderOrder.getProductType(), resellerFundAccount.getResellerNo());
    CalcAmount calcAmount = calcChangeRecoveriesAmount(pretenderOrder, resellerFundAccount,
        resellerPretenderProduct);
    ResellerFundLine resellerFundLine = buildResellerFundLine(pretenderOrder, calcAmount);
    boolean isSuc = resellerFundLineService.save(resellerFundLine);
    if (!isSuc) {
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }
    saveResellerProductRecoveries(resellerPretenderProduct, calcAmount);
    saveResellerFundAccount(resellerFundAccount, calcAmount);
    saveTodayResellerFundAccount(resellerFundAccount.getResellerNo(), calcAmount);
  }

  private void checkParam(PretenderOrder pretenderOrder) {
    if (pretenderOrder == null ||
        !StringUtils.equalsIgnoreCase(pretenderOrder.getStatus(),
            PretenderOrderStatusEnum.FINISH.getCode())) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
  }

  private ResellerOrder queryResellerOrder(String resellerOrderNo) {
    ResellerOrder resellerOrder = resellerOrderService
        .getOne(ResellerOrder.gw().eq(ResellerOrder::getOrderNo, resellerOrderNo));
    if (resellerOrder == null) {
      throw new BizException(ApiCodeEnum.NO_RESELLER_ORDER);
    }
    return resellerOrder;
  }


  private ResellerFundAccount queryResellerAccount(String resellerNo) {
    ResellerFundAccount resellerFundAccount = resellerFundAccountService
        .getOne(ResellerFundAccount.gw().eq(ResellerFundAccount::getResellerNo, resellerNo));
    if (resellerFundAccount == null) {
      throw new BizException(ApiCodeEnum.RESELLER_ACCOUNT_NOT_EXIST);
    }
    return resellerFundAccount;
  }


  private ResellerFundLine buildResellerFundLine(PretenderOrder pretenderOrder,
      CalcAmount calcAmount) {
    ResellerFundLine resellerFundLine = new ResellerFundLine();
    resellerFundLine.setResellerNo(calcAmount.getResellerNo());
    resellerFundLine.setProductType(pretenderOrder.getProductType());
    resellerFundLine.setBeforeRecoveriesAmount(calcAmount.getBeforeRecoveriesAmount());
    resellerFundLine.setAfterRecoveriesAmount(calcAmount.getAfterRecoveriesAmount());
    resellerFundLine.setResellerShareAmount(calcAmount.getResellerShareAmount());
    resellerFundLine.setChangeRecoveriesAmount(calcAmount.getChangeRecoveriesAmount());
    resellerFundLine.setOrderAmount(pretenderOrder.getAmount());
    resellerFundLine.setProductOrderNo(pretenderOrder.getMatchResellerOrderNo());
    resellerFundLine.setBizType(FundLineBizType.SHARE.getCode());
    return resellerFundLine;
  }


  private CalcAmount calcChangeRecoveriesAmount(PretenderOrder pretenderOrder,
      ResellerFundAccount resellerFundAccount, ResellerPretenderProduct resellerPretenderProduct) {
    CalcAmount calcAmount = new CalcAmount();
    //calc the fund line
    Long changeRecoveriesAmount = AmountUtil
        .calPercentageFee(pretenderOrder.getAmount(), resellerPretenderProduct.getFeeRate());
    Long resellerShareAmount = pretenderOrder.getAmount() - changeRecoveriesAmount;
    Long beforeRecoveriesAmount = resellerFundAccount.getRecoveriesAllAmount();
    Long afterRecoveriesAmount =
        resellerFundAccount.getRecoveriesAllAmount() + changeRecoveriesAmount;
    calcAmount.setChangeRecoveriesAmount(changeRecoveriesAmount);
    calcAmount.setResellerShareAmount(resellerShareAmount);
    calcAmount.setBeforeRecoveriesAmount(beforeRecoveriesAmount);
    calcAmount.setAfterRecoveriesAmount(afterRecoveriesAmount);
    //calc the general ledger
    Long recoveriesAllAmount =
        resellerFundAccount.getRecoveriesAllAmount() + changeRecoveriesAmount;
    Long finishedAllAmount =
        resellerFundAccount.getFinishedAllAmount() + pretenderOrder.getAmount();
    Long waitAllAmount = resellerFundAccount.getWaitAllAmount() - pretenderOrder.getAmount();
    Long shareAllAmount = resellerFundAccount.getShareAllAmount() + resellerShareAmount;
    Long allAmount = resellerFundAccount.getAllAmount();
    calcAmount.setRecoveriesAllAmount(recoveriesAllAmount);
    calcAmount.setFinishedAllAmount(finishedAllAmount);
    calcAmount.setWaitAllAmount(waitAllAmount);
    calcAmount.setShareAllAmount(shareAllAmount);
    calcAmount.setAllAmount(allAmount);
    calcAmount.setResellerNo(resellerFundAccount.getResellerNo());
    return calcAmount;
  }


  private ResellerPretenderProduct queryResellerPretenderProduct(String productType,
      String resellerNo) {
    ResellerPretenderProduct resellerPretenderProduct = resellerPretenderProductService.getOne(
        ResellerPretenderProduct.gw().eq(ResellerPretenderProduct::getProductType, productType)
            .eq(ResellerPretenderProduct::getResellerNo, resellerNo));
    if (resellerPretenderProduct == null) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
    return resellerPretenderProduct;
  }

  private void saveResellerProductRecoveries(ResellerPretenderProduct resellerPretenderProduct,
      CalcAmount calcAmount) {
    long recoveriesAmount =
        resellerPretenderProduct.getRecoveriesAmount() + calcAmount.getChangeRecoveriesAmount();
    resellerPretenderProduct.setRecoveriesAmount(recoveriesAmount);
    resellerPretenderProductService.update(new LambdaUpdateWrapper<ResellerPretenderProduct>()
        .set(ResellerPretenderProduct::getRecoveriesAmount, recoveriesAmount)
        .eq(ResellerPretenderProduct::getId, resellerPretenderProduct.getId()));
    if (resellerPretenderProduct.getRecoveriesAmount() >= resellerPretenderProduct
        .getCreditAmount()) {
      resellerOrderService
          .update(new LambdaUpdateWrapper<ResellerOrder>().set(ResellerOrder::getOrderStatus,
              ResellerOrderStatusEnum.NULLIFY.getCode())
              .eq(ResellerOrder::getResellerNo, resellerPretenderProduct.getResellerNo())
              .eq(ResellerOrder::getOrderStatus,
                  ResellerOrderStatusEnum.WAITING_PAY.getCode()));
      resellerPretenderProductService.update(new LambdaUpdateWrapper<ResellerPretenderProduct>()
          .set(ResellerPretenderProduct::getStatus, SwitchStatusEnum.DISABLE.getCode())
          .eq(ResellerPretenderProduct::getId, resellerPretenderProduct.getId()));
    }
  }

  private void saveResellerFundAccount(ResellerFundAccount resellerFundAccount,
      CalcAmount calcAmount) {
    resellerFundAccount.setAllAmount(calcAmount.getAllAmount());
    resellerFundAccount.setRecoveriesAllAmount(calcAmount.getRecoveriesAllAmount());
    resellerFundAccount.setFinishedAllAmount(calcAmount.getFinishedAllAmount());
    resellerFundAccount.setWaitAllAmount(calcAmount.getWaitAllAmount());
    resellerFundAccount.setShareAllAmount(calcAmount.getShareAllAmount());
    resellerFundAccount.setGmtUpdate(new Date());
    boolean isSuc = resellerFundAccountService.updateById(resellerFundAccount);
    if (!isSuc) {
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }
  }

  private void saveTodayResellerFundAccount(String resellerNo,
      CalcAmount calcAmount) {
    ResellerFundAccountSnapshot resellerFundAccountSnapshot = queryTodayResellerSnapshot(
        resellerNo);
    resellerFundAccountSnapshot.setAllAmount(calcAmount.getAllAmount());
    resellerFundAccountSnapshot.setRecoveriesAllAmount(calcAmount.getRecoveriesAllAmount());
    resellerFundAccountSnapshot.setWaitAllAmount(calcAmount.getWaitAllAmount());
    resellerFundAccountSnapshot.setFinishedAllAmount(calcAmount.getFinishedAllAmount());
    resellerFundAccountSnapshot.setShareAllAmount(calcAmount.getShareAllAmount());
    boolean isSuc = resellerFundAccountSnapshotService.saveOrUpdate(resellerFundAccountSnapshot);
    if (!isSuc) {
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }
  }

  private ResellerFundAccountSnapshot queryTodayResellerSnapshot(String resellerNo) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date now = new Date();
    //before time
    Calendar before = Calendar.getInstance();
    before.setTime(now);
    before.add(Calendar.DAY_OF_MONTH, -1);
    //after time
    Calendar after = Calendar.getInstance();
    after.setTime(now);
    after.add(Calendar.DAY_OF_MONTH, 1);
    ResellerFundAccountSnapshot resellerFundAccountSnapshot = resellerFundAccountSnapshotService
        .getOne(ResellerFundAccountSnapshot.gw()
            .eq(ResellerFundAccountSnapshot::getResellerNo, resellerNo)
            .gt(ResellerFundAccountSnapshot::getGmtCreate, sdf.format(before.getTime()))
            .lt(ResellerFundAccountSnapshot::getGmtCreate, sdf.format(after.getTime())
            ));
    //if today snapshot is not exist,build new
    if (resellerFundAccountSnapshot == null) {
      resellerFundAccountSnapshot = new ResellerFundAccountSnapshot();
    }
    resellerFundAccountSnapshot.setResellerNo(resellerNo);
    return resellerFundAccountSnapshot;
  }

  @Data
  private static class CalcAmount {

    private Long beforeRecoveriesAmount;
    private Long changeRecoveriesAmount;
    private Long afterRecoveriesAmount;
    private Long resellerShareAmount;

    /**
     * 核销商所有订单金额，导入的时候进行增加,单位分
     */
    private Long allAmount;

    /**
     * 回款金额
     */
    private Long recoveriesAllAmount;

    /**
     * 所有完成订单总金额，支付订单做加减或者直接统计，单位分
     */
    private Long finishedAllAmount;

    /**
     * 所有待充值总金额，支付订单时做加减或者直接统计，单位分
     */
    private Long waitAllAmount;

    /**
     * 核销商所有分享金额
     */
    private Long shareAllAmount;

    private String resellerNo;
  }


}
