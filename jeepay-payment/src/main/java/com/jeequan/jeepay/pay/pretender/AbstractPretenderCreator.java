package com.jeequan.jeepay.pay.pretender;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import java.util.Optional;
import java.util.concurrent.*;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.model.params.ProxyParams;
import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import com.jeequan.jeepay.pay.pretender.proxy.ProxyIpHunter;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.pay.pretender.model.ProductFacePrice;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.*;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.constants.PretenderOrderStatusEnum;
import com.jeequan.jeepay.core.constants.PretenderAccountStatusEnum;
import com.jeequan.jeepay.service.biz.PretenderAccountUseStatisticsRecorder;

/**
 * @author axl rose
 * @date 2021/9/12
 */
@Slf4j
public abstract class AbstractPretenderCreator implements PretenderOrderCreator {

  private static final Logger logger = LoggerFactory.getLogger(PretenderOrderCreator.class);

  @Autowired
  protected PretenderAccountService pretenderAccountService;

  @Autowired
  protected ResellerOrderService resellerOrderService;

  @Autowired
  protected PretenderOrderService pretenderOrderService;

  @Autowired
  protected ProxyIpHunter proxyIpHunter;

  @Autowired
  protected PretenderAccountUseStatisticsRecorder pretenderAccountUseStatisticsRecorder;

  /**
   * 订单处理异步处理线程池
   */
  private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
      .setNameFormat("pretender-order-creator-thread-call-runner-%d").build();

  protected ExecutorService taskExec = new ThreadPoolExecutor(10, 20, 200L, TimeUnit.MILLISECONDS,
      new LinkedBlockingDeque<>(), namedThreadFactory);

  /**
   * 错误日志记录处理线程池
   */
  private ThreadFactory failedNamedThreadFactory = new ThreadFactoryBuilder()
      .setNameFormat("pretender-order-save-failed-thread-call-runner-%d").build();

  protected ExecutorService failedTaskExec = new ThreadPoolExecutor(10, 20, 200L,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingDeque<>(), failedNamedThreadFactory);


  @Override
  @Transactional(rollbackFor = Exception.class)
  public PretenderOrder createOrder(BaseRq baseRq) {
    //get bizType from concrete
    String bizType = getBizType();
    //get productType from concrete
    String productType = getProductTypeEnum().getCode();
    logger.info("start [AbstractPretenderCreator.createOrder],bizType={},productType={},baseRq={}",
        bizType, productType, baseRq);
    //check param
    checkBaseRq(baseRq);
    //first, find the matched charge face price,if is not exist ,throw biz exception
    ProductFacePrice facePrice = matchTheAvailablePrice(baseRq);
    // random pretender account
    PretenderAccount pretenderAccount = randomPretenderAccountByBizType(bizType);
    //random  reseller order
    ResellerOrder resellerOrder = randomMatchedResellerOrder(baseRq.getChargeAmount(), productType);
    try {
      // update reseller order matching status first,for optimistic lock
      updateResellerOrderToMatch(resellerOrder);
      // invoke subclass  do concrete create order
      PretenderOrder pretenderOrder = doCreateOrder(resellerOrder, pretenderAccount, facePrice);
      //save the pretender order
      savePretenderOrder(pretenderOrder);
      logger.info(
          "end [AbstractPretenderCreator.createOrder], success bizType={},productType={},baseRq={},pretenderOrder={}",
          bizType,
          productType, baseRq, pretenderOrder);
      //record the pretender account  log
      pretenderAccountUseStatisticsRecorder.recorder(pretenderOrder);
      return pretenderOrder;
    } catch (Exception e) {
      //save failed pretender log
      failedTaskExec.execute(() -> saveFailedPretenderOrder(resellerOrder, pretenderAccount));
      throw e;
    }
  }

  /**
   * check param and invoke the sub creator extendParamCheck
   *
   * @param baseRq
   */
  public void checkBaseRq(BaseRq baseRq) {
    if (baseRq == null ||
        baseRq.getChargeAmount() == null || !extendParamCheck(baseRq)) {
      throw new BizException(PARAMS_ERROR);
    }
  }

  /**
   * extend param check for sub creator
   *
   * @param baseRq
   * @return boolean
   */
  protected abstract boolean extendParamCheck(BaseRq baseRq);

  /**
   * get the charge face price amount-> discount
   *
   * @return FacePrice face price
   */
  protected abstract List<ProductFacePrice> getAvailableFacePrice();

  /**
   * get  biz type
   *
   * @return String
   */
  protected abstract String getBizType();


  /**
   * get  product type
   *
   * @return ProductTypeEnum
   */
  protected abstract ProductTypeEnum getProductTypeEnum();


  /**
   * do really order create
   *
   * @param resellerOrder    reseller order ,user for create pretender order
   * @param pretenderAccount pretender account for create pretender account
   * @param facePrice        face price of the product order
   * @return JSONObject
   */
  protected abstract PretenderOrder doCreateOrder(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount, ProductFacePrice facePrice);

  /**
   * get the pay way
   *
   * @return String pay way like alipay-h5 or wechat-h5
   */
  protected abstract String getPayWay();


  protected ProxyParams getProxy() {
    return proxyIpHunter.huntProxy();
  }

  /**
   * matched the available price
   *
   * @param baseRq param
   * @return FacePrice
   */
  private ProductFacePrice matchTheAvailablePrice(BaseRq baseRq) {
    List<ProductFacePrice> facePriceList = getAvailableFacePrice();
    //find match amount product Face Price
    Optional<ProductFacePrice> matchedFacePriceOpt = facePriceList.stream()
        .filter(facePrice -> facePrice.getFacePrice().equals(baseRq.getChargeAmount())).findAny();
    //if exist return
    if (matchedFacePriceOpt.isPresent()) {
      return matchedFacePriceOpt.get();
    }
    // has no matched face price,find whether exist custom face price
    Optional<ProductFacePrice> customFacePriceOpt = facePriceList.stream().
        filter(ProductFacePrice::isCustom).findAny();
    //exist custom face price
    if (customFacePriceOpt.isPresent()) {
      //if charge amount greater than custom face price limit price,throw amount exception
      if (baseRq.getChargeAmount() > customFacePriceOpt.get().getLimitPrice()) {
        throw new BizException(ORDER_CHARGE_AMOUNT_ILLEGAL);
      }
      return customFacePriceOpt.get();
    }
    //no match face price and no custom face price ,throw amount exception
    throw new BizException(ORDER_CHARGE_AMOUNT_ILLEGAL);
  }

  /**
   * random available pretender account by biz Type
   *
   * @param bizType biz type
   * @return Pretender account
   */
  private PretenderAccount randomPretenderAccountByBizType(String bizType) {
    /**
     * random a pretender account by biz type
     */
    PretenderAccount pretenderAccount = pretenderAccountService.randomByBizType(bizType);
    //is no exist,throw NO_PRETENDER_ACCOUNT exception
    if (pretenderAccount == null) {
      throw new BizException(NO_PRETENDER_ACCOUNT);
    }
    return pretenderAccount;
  }

  /**
   * random matched  reseller order ,if it is not exist ,throw biz exception random find a matched
   * reseller order
   *
   * @param chargeAmount charge amount
   * @param productType  productType
   * @return ResellerOrder
   */
  private ResellerOrder randomMatchedResellerOrder(Long chargeAmount, String productType) {
    ResellerOrder resellerOrder = resellerOrderService.randomByAmountAndProductType(chargeAmount,
        productType);
    //if reseller order not exist
    if (resellerOrder == null) {
      throw new BizException(NO_RESELLER_ORDER);
    }
    return resellerOrder;
  }

  /**
   * update reseller order matching,if you can not update success,throw exception
   *
   * @param resellerOrder
   */
  public void updateResellerOrderToMatch(ResellerOrder resellerOrder) {
    ResellerOrder newResellerOrder = new ResellerOrder();
    newResellerOrder.setVersion(resellerOrder.getVersion());
    newResellerOrder.setOrderStatus(ResellerOrderStatusEnum.MATCHING.getCode());
    newResellerOrder.setId(resellerOrder.getId());
    newResellerOrder.setGmtUpdate(new Date());
    boolean isSuc = resellerOrderService.updateById(newResellerOrder);
    if (!isSuc) {
      throw new BizException(SYS_OPERATION_FAIL_UPDATE);
    }
  }

  public void savePretenderOrder(PretenderOrder pretenderOrder) {
    boolean isSaveSuccess = pretenderOrderService.save(pretenderOrder);
    if (!isSaveSuccess) {
      logger.error(
          "[AbstractPropertyCreditOrderCreator.savePretenderOrder] failed ,save pretender order failed,pretenderAccount={}",
          pretenderOrder.getPretenderAccountId());
      throw new BizException(SYS_OPERATION_FAIL_CREATE);
    }
  }

  private void saveFailedPretenderOrder(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount) {
    PretenderOrder pretenderOrder = buildFailedPretenderOrder(resellerOrder, pretenderAccount);
    pretenderOrderService.save(pretenderOrder);
    pretenderAccountUseStatisticsRecorder.recorder(pretenderOrder);
  }

  private PretenderOrder buildFailedPretenderOrder(ResellerOrder resellerOrder,
      PretenderAccount pretenderAccount) {
    PretenderOrder pretenderOrder = new PretenderOrder();
    pretenderOrder.setAmount(resellerOrder.getAmount());
    pretenderOrder.setGmtCreate(new Date());
    pretenderOrder.setBizType(getBizType());
    pretenderOrder.setPretenderAccountId(pretenderAccount.getId());
    pretenderOrder.setMatchResellerOrderNo(resellerOrder.getOrderNo());
    pretenderOrder.setPayWay(getPayWay());
    pretenderOrder.setStatus(PretenderOrderStatusEnum.REQUEST_FAILED.getCode());
    pretenderOrder.setProductType(getProductTypeEnum().getCode());
    return pretenderOrder;
  }

  @Override
  public boolean hasAvailablePretenderAccount(BaseRq baseRq) {
    //check whether exist available pretender account
    int count = pretenderAccountService.count(PretenderAccount.gw().
        eq(PretenderAccount::getBizType, getBizType()).
        eq(PretenderAccount::getStatus,
            PretenderAccountStatusEnum.AVAILABLE.getCode()));
    return count >= 0;
  }

  @Override
  public boolean hasAvailableResellerOrder(BaseRq baseRq) {
    //check whether exist available reseller order
    int count = resellerOrderService.count(ResellerOrder.gw().
        eq(ResellerOrder::getAmount, baseRq.getChargeAmount()).
        eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.WAITING_MATCH)
        .eq(ResellerOrder::getProductType, getProductTypeEnum().getCode()));
    return count >= 0;
  }

}
