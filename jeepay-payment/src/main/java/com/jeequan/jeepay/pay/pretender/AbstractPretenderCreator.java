package com.jeequan.jeepay.pay.pretender;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.pay.pretender.model.FacePrice;
import com.jeequan.jeepay.pay.pretender.rq.BaseRq;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.*;

/**
 * @author axl rose
 * @date 2021/9/12
 */
@Slf4j
public abstract class AbstractPretenderCreator implements PretenderOrderCreator {

    private static final Logger logger = LoggerFactory.getLogger(PretenderOrderCreator.class);

    @Autowired protected PretenderAccountService pretenderAccountService;

    @Autowired protected ResellerOrderService resellerOrderService;

    @Autowired protected PretenderOrderService pretenderOrderService;

    /**
     * 异步处理线程池
     */
    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("pretender-order-creator-thread-call-runner-%d").build();

    protected ExecutorService taskExec = new ThreadPoolExecutor(10,20,200L, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),namedThreadFactory);


    @Override
    public PretenderOrder createOrder(BaseRq baseRq) {
        String bizType = getBizType();
        String productType = getProductTypeEnum().getCode();
        logger.info("start [AbstractPretenderCreator.createOrder],bizType={},productType={},baseRq={}", bizType, productType, baseRq);
        //check param
        checkBaseRq(baseRq);
        //first, find the matched charge face price,if is not exist ,throw biz exception
        FacePrice facePrice = matchTheAvailablePrice(baseRq);
        PretenderAccount pretenderAccount =findPretenderAccountByBizType(bizType);
        ResellerOrder resellerOrder  = findMatchedResellerOrder(baseRq.getChargeAmount(),productType);
        //do create order
        PretenderOrder pretenderOrder = doCreateOrder(resellerOrder,pretenderAccount,facePrice);
        //update the reseller order status to "PAYING"
        savePretenderOrder(pretenderOrder);
        logger.info("end [AbstractPretenderCreator.createOrder], success bizType={},productType={},baseRq={},pretenderOrder={}", bizType,
                productType, baseRq,pretenderOrder);
        return pretenderOrder;
    }

    public void checkBaseRq(BaseRq baseRq) {
        if (baseRq == null ||
                baseRq.getChargeAmount() == null || !extendParamCheck(baseRq)) {
            throw new BizException(PARAMS_ERROR);
        }
    }

   protected abstract boolean extendParamCheck(BaseRq baseRq);

    /**
     * get the charge face price
     * amount-> discount
     *
     * @return FacePrice face price
     */
    protected abstract List<FacePrice> getAvailableFacePrice();

    /**
     * get  biz type
     *
     * @return String
     */
    protected   abstract String getBizType();


    /**
     * get  product type
     *
     * @return ProductTypeEnum
     */
    protected abstract ProductTypeEnum getProductTypeEnum();


    /**
     *   do really order create
     * @param resellerOrder reseller order ,user for create pretender order
     * @param pretenderAccount pretender account for create pretender account
     * @param facePrice face price of the product order
     * @return JSONObject
     */
    protected abstract PretenderOrder doCreateOrder(ResellerOrder resellerOrder, PretenderAccount pretenderAccount, FacePrice facePrice);

    /**
     * get the pay way
     * @return String pay way like alipay-h5 or wechat-h5
     */
    protected   abstract String getPayWay();

    /**
     * matched the available price
     *
     * @param baseRq param
     * @return FacePrice
     */
    private FacePrice matchTheAvailablePrice(BaseRq baseRq) {
        List<FacePrice> facePriceList = getAvailableFacePrice();
        Optional<FacePrice> matchedFacePriceOpt = facePriceList.stream().filter(facePrice -> facePrice.getFacePrice().equals(baseRq.getChargeAmount())).findAny();
        if(matchedFacePriceOpt.isPresent()){
            return matchedFacePriceOpt.get();
        }
        Optional<FacePrice> customFacePriceOpt = facePriceList.stream().
                filter(FacePrice::isCustom).findAny();
        if (customFacePriceOpt.isPresent()) {
            if (baseRq.getChargeAmount() > customFacePriceOpt.get().getLimitPrice()) {
                throw new BizException(ORDER_CHARGE_AMOUNT_ILLEGAL);
            }
        }
        if (!customFacePriceOpt.isPresent()) {
            throw new BizException(ORDER_CHARGE_AMOUNT_ILLEGAL);
        }
        return customFacePriceOpt.get();
    }

    /**
     *
     * find available pretender account by biz Type
     * @param  bizType biz type
     * @return Pretender account
     */
    private PretenderAccount findPretenderAccountByBizType(String bizType){
        PretenderAccount pretenderAccount = pretenderAccountService.randomByBizType(bizType);
        if(pretenderAccount==null){
            throw new BizException(NO_PRETENDER_ACCOUNT);
        }
        return pretenderAccount;
    }

    /**
     * find matched  reseller order ,if it is not exist ,throw biz exception
     * random find a matched reseller order
     * @param  chargeAmount charge amount
     * @param productType productType
     * @return ResellerOrder
     */
    private ResellerOrder  findMatchedResellerOrder(Long chargeAmount,String productType){
        ResellerOrder resellerOrder  =resellerOrderService.getOne(ResellerOrder.gw().eq(ResellerOrder::getAmount,chargeAmount).
                eq(ResellerOrder::getProductType,productType).eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.WAITING_PAY),false );
        if(resellerOrder==null){
            throw new BizException(NO_RESELLER_ORDER);
        }
        return  resellerOrder;
    }

    public void savePretenderOrder(PretenderOrder pretenderOrder){
        boolean isSaveSuccess = pretenderOrderService.save(pretenderOrder);
        if (!isSaveSuccess) {
            logger.error("[AbstractPropertyCreditOrderCreator.savePretenderOrder] failed ,save pretender order failed,pretenderAccount={}", pretenderOrder.getPretenderAccountId());
            throw new BizException(SYS_OPERATION_FAIL_CREATE);
        }
    }

}
