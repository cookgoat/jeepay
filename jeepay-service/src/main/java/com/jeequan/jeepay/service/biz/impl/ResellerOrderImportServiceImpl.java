package com.jeequan.jeepay.service.biz.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.core.entity.PretenderProduct;
import com.jeequan.jeepay.service.impl.PretenderProductService;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import com.jeequan.jeepay.core.constants.AccountStatusEnum;
import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import com.jeequan.jeepay.core.entity.ResellerFundAccount;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;
import com.jeequan.jeepay.service.impl.ResellerFundAccountService;
import com.jeequan.jeepay.service.impl.ResellerPretenderProductService;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderChargeAccountType;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.Reseller;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.ExcelUtil;
import com.jeequan.jeepay.core.utils.SnowflakeIdWorker;
import com.jeequan.jeepay.service.biz.ResellerOrderImportService;
import com.jeequan.jeepay.service.biz.fileentity.ResellerOrderBaseExcelFileEntity;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import com.jeequan.jeepay.service.biz.rq.ResellerOrderImportRequest;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.service.impl.ResellerService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import static com.jeequan.jeepay.core.utils.RegexUtil.isMobile;
import static com.jeequan.jeepay.core.utils.RegexUtil.isInteger;
import static com.jeequan.jeepay.core.constants.ProductTypeEnum.isRightProductType;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Service
public class ResellerOrderImportServiceImpl implements ResellerOrderImportService {

  private static final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(1, 1);
  @Autowired
  private ResellerOrderService resellerOrderService;


  @Autowired
  private ResellerService resellerService;

  @Autowired
  private ResellerPretenderProductService resellerPretenderProductService;

  @Autowired
  private ResellerFundAccountService resellerFundAccountService;

  @Autowired
  private PretenderProductService pretenderProductService;

  @Override
  public void batchImport(ResellerOrderImportRequest resellerImportRequest) {
    try {
      checkParam(resellerImportRequest);
      checkProduct(resellerImportRequest);
      Reseller reseller = queryReseller(resellerImportRequest);
      checkResellerStatus(reseller);
      //parse orders from excel
      List<ResellerOrderBaseExcelFileEntity> resellerOrderBaseExcelFileEntities =
          ExcelUtil.importExcel(resellerImportRequest.getMultipartFile(), 0, 1,
              ResellerOrderBaseExcelFileEntity.class);
      List<ResellerOrder> resellerOrderList = buildResellerOrderList(resellerImportRequest,
          resellerOrderBaseExcelFileEntities);
      if (resellerOrderList == null || resellerOrderList.size() <= 0) {
        return;
      }
      ResellerPretenderProduct resellerPretenderProduct = queryResellerPretenderProduct(
          resellerImportRequest, reseller);
      ResellerFundAccount resellerFundAccount = queryResellerFundAccount(reseller);
      checkCreditAmountLimit(resellerFundAccount, resellerPretenderProduct, resellerOrderList);
      boolean isSuccess = resellerOrderService.saveBatch(resellerOrderList);

      if (!isSuccess) {
        throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BizException(e.getMessage());
    }
  }

  private List<ResellerOrder> buildResellerOrderList(
      ResellerOrderImportRequest resellerImportRequest,
      List<ResellerOrderBaseExcelFileEntity> resellerOrderBaseExcelFileEntities) {
    List<ResellerOrder> resellerOrderList = new ArrayList<>();
    if (resellerOrderBaseExcelFileEntities != null
        && resellerOrderBaseExcelFileEntities.size() > 0) {
      for (ResellerOrderBaseExcelFileEntity resellerOrderBaseExcelFileEntity : resellerOrderBaseExcelFileEntities) {
        ResellerOrder resellerOrder = convert(resellerImportRequest,
            resellerOrderBaseExcelFileEntity);
        if (resellerOrder == null) {
          continue;
        }
        resellerOrderList.add(resellerOrder);
      }
    }
    return resellerOrderList;
  }

  private ResellerOrder convert(ResellerOrderImportRequest resellerImportRequest,
      ResellerOrderBaseExcelFileEntity resellerOrderBaseExcelFileEntity) {
    Reseller reseller = queryReseller(resellerImportRequest);
    ResellerOrder resellerOrder = new ResellerOrder();
    resellerOrder.setResellerNo(reseller.getResellerNo());

    if (StringUtils.isBlank(resellerOrderBaseExcelFileEntity.getOrderNo())) {
      resellerOrder.setOrderNo(generateOrderNo());
    } else {
      int count = resellerOrderService.count(ResellerOrder.gw()
          .eq(ResellerOrder::getOrderNo, resellerOrderBaseExcelFileEntity.getOrderNo()));
      if (count > 0) {
        return null;
      }
      resellerOrder.setOrderNo(resellerOrderBaseExcelFileEntity.getOrderNo());
    }
    if (StringUtils.isBlank(resellerOrderBaseExcelFileEntity.getAmount()) || !isInteger(
        resellerOrderBaseExcelFileEntity.getAmount())) {
      return null;
    }
    resellerOrder.setAmount(
        Long.valueOf(AmountUtil.convertDollar2Cent(resellerOrderBaseExcelFileEntity.getAmount())));

    if (StringUtils.isBlank(resellerOrderBaseExcelFileEntity.getChargeAccount())) {
      return null;
    }
    resellerOrder.setChargeAccount(resellerOrderBaseExcelFileEntity.getChargeAccount());
    resellerOrder.setQueryFlag(resellerOrderBaseExcelFileEntity.getQueryFlag());
    resellerOrder.setProductType(resellerImportRequest.getProductType());
    resellerOrder.setGmtCreate(new Date());
    if (isMobile(resellerOrderBaseExcelFileEntity.getChargeAccount())) {
      resellerOrder.setChargeAccountType(ResellerOrderChargeAccountType.MOBILE.getCode());
    } else {
      resellerOrder.setChargeAccountType(ResellerOrderChargeAccountType.PLATFORM_ACCOUNT.getCode());
    }
    resellerOrder.setOrderStatus(ResellerOrderStatusEnum.WAITING_MATCH.getCode());
    return resellerOrder;
  }

  private String generateOrderNo() {
    return "RO" + snowflakeIdWorker.nextId();
  }


  private void checkParam(ResellerOrderImportRequest resellerOrderImportRequest) {
    if (resellerOrderImportRequest == null ||
        StringUtils.isBlank(resellerOrderImportRequest.getProductType()) ||
        resellerOrderImportRequest.getCurrentUserId() == 0 ||
        resellerOrderImportRequest.getMultipartFile() == null ||
        !isRightProductType(resellerOrderImportRequest.getProductType())) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
  }


  private Reseller queryReseller(ResellerOrderImportRequest resellerOrderImportRequest) {
    Reseller reseller = resellerService.getOne(
        Reseller.gw().eq(Reseller::getInitUserId, resellerOrderImportRequest.getCurrentUserId()));
    if (reseller == null) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
    return reseller;
  }

  private void checkResellerStatus(Reseller reseller) {
    if (!StringUtils.equalsIgnoreCase(reseller.getStatus(), AccountStatusEnum.ENABLE.getCode())) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
  }

  private void checkProduct(ResellerOrderImportRequest resellerOrderImportRequest) {
    int count = pretenderProductService.count(PretenderProduct.gw()
        .eq(PretenderProduct::getProductType, resellerOrderImportRequest.getProductType())
        .eq(PretenderProduct::getStatus, SwitchStatusEnum.ENABLE));
    if (count <= 0) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
  }

  private ResellerPretenderProduct queryResellerPretenderProduct(
      ResellerOrderImportRequest resellerOrderImportRequest,
      Reseller reseller) {
    ResellerPretenderProduct resellerPretenderProduct = resellerPretenderProductService
        .getOne(ResellerPretenderProduct.gw()
            .eq(ResellerPretenderProduct::getResellerNo, reseller.getResellerNo())
            .eq(ResellerPretenderProduct::getProductType,
                resellerOrderImportRequest.getProductType())
            .eq(
                ResellerPretenderProduct::getStatus, SwitchStatusEnum.ENABLE
            ));
    if (resellerPretenderProduct == null) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
    return resellerPretenderProduct;
  }

  private ResellerFundAccount queryResellerFundAccount(Reseller reseller) {
    ResellerFundAccount resellerFundAccount = resellerFundAccountService.getOne(
        ResellerFundAccount.gw().eq(ResellerFundAccount::getResellerNo, reseller.getResellerNo()));
    if (resellerFundAccount == null) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }
    return resellerFundAccount;
  }

  private void checkCreditAmountLimit(ResellerFundAccount resellerFundAccount,
      ResellerPretenderProduct resellerPretenderProduct,
      List<ResellerOrder> resellerOrderList) {
    long orderSunAmount = resellerOrderList.stream()
        .collect(Collectors.summingLong(ResellerOrder::getAmount));
    if (orderSunAmount > resellerPretenderProduct.getCreditAmount()
        || (orderSunAmount + resellerFundAccount.getWaitAllAmount()) > resellerPretenderProduct
        .getCreditAmount()
        || resellerPretenderProduct.getRecoveriesAmount() > resellerPretenderProduct
        .getCreditAmount()) {
      throw new BizException(ApiCodeEnum.PARAMS_ERROR);
    }

    long allAmount = orderSunAmount + resellerFundAccount.getAllAmount();
    long allWaitAmount = orderSunAmount + resellerFundAccount.getWaitAllAmount();
    boolean isSuc = resellerFundAccountService.update(new LambdaUpdateWrapper<ResellerFundAccount>()
        .set(ResellerFundAccount::getAllAmount, allAmount)
        .set(ResellerFundAccount::getWaitAllAmount, allWaitAmount));
    if (!isSuc) {
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }
  }


}
