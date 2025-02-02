package com.jeequan.jeepay.pay.channel.pretender;


import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.constants.PretenderOrderStatusEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.DateUtil;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.service.biz.ResellerFundLineRecorder;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.PropertyCreditUtil;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rs.QueryOrderResult;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.QueryOrderRequest;

import static com.jeequan.jeepay.core.constants.ApiCodeEnum.QUERY_PRETENDER_ORDER_FAILED;

/**
 *
 */
@Service("pretenderpayPayOrderQueryService")
public class PretenderpayPayOrderQueryService implements IPayOrderQueryService {

  @Autowired
  private ResellerOrderService resellerOrderService;

  @Autowired
  private PretenderOrderService pretenderOrderService;

  @Autowired
  private PretenderAccountService pretenderAccountService;

  @Autowired
  ResellerFundLineRecorder resellerFundLineRecorder;


  @Autowired
  private SysConfigService sysConfigService;

  /**
   * 异步处理线程池
   */
  private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
      .setNameFormat("pretender-order-query-thread-call-runner-%d").build();

  protected ExecutorService taskExec = new ThreadPoolExecutor(10, 20, 200L, TimeUnit.MILLISECONDS,
      new LinkedBlockingDeque<>(), namedThreadFactory);


  @Override
  public String getIfCode() {
    return CS.IF_CODE.PRETENDERPAY;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) {
    long distanceMinute = DateUtil.getDistanceMinute(payOrder.getCreatedAt(), new Date());
    PretenderOrder pretenderOrder = pretenderOrderService
        .getOne(PretenderOrder.gw().eq(PretenderOrder::getOutTradeNo, payOrder.getChannelOrderNo())
            .eq(PretenderOrder::getStatus, PretenderOrderStatusEnum.PAYING), true);
    if (pretenderOrder == null && distanceMinute <= 2) {
      return ChannelRetMsg.waiting();
    }
    if (pretenderOrder == null && distanceMinute > 2) {
      return ChannelRetMsg.confirmFail();
    }

    QueryOrderRequest queryOrderRequest = new QueryOrderRequest();
    queryOrderRequest.setOrderNo(pretenderOrder.getOutTradeNo());
    PretenderAccount pretenderAccount = pretenderAccountService.getOne(
        PretenderAccount.gw().eq(PretenderAccount::getId, pretenderOrder.getPretenderAccountId()),
        false);
    queryOrderRequest.setCookie(pretenderAccount.getCertificate());
    PropertyCreditUtil propertyCreditUtil = new PropertyCreditUtil();
    QueryOrderResult queryOrderResult = propertyCreditUtil.queryOrder(queryOrderRequest);

    if (!queryOrderResult.isSuccess() || queryOrderResult.getData() == null) {
      throw new BizException(QUERY_PRETENDER_ORDER_FAILED);
    }
    String notifyStatus = sysConfigService.getDBApplicationConfig().getPretenderNotifyStatus();
    ResellerOrder resellerOrder = resellerOrderService
        .getOne(ResellerOrder.gw()
            .eq(ResellerOrder::getOrderNo, pretenderOrder.getMatchResellerOrderNo())
            .eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.PAYING), true);
    if (StringUtils.equalsIgnoreCase(notifyStatus, queryOrderResult.getData().getStatus()) ||
        queryOrderResult.getData().isSuccess()) {
      doSuccess(resellerOrder, pretenderOrder, payOrder);
      //todo use mq or thread
      taskExec.execute(() -> resellerFundLineRecorder.record(pretenderOrder));
      return ChannelRetMsg.confirmSuccess(queryOrderResult.getData().getOrderNo());
    }
    if (queryOrderResult.getData().isWaiting()) {
      return ChannelRetMsg.waiting();
    }
    if (isExpire(resellerOrder, distanceMinute)) {
      doExpireOperation(resellerOrder, pretenderOrder);
      return ChannelRetMsg.confirmCancel(queryOrderResult.getData().getOrderNo());
    }
    return ChannelRetMsg.unknown();
  }

  private boolean isExpire(ResellerOrder resellerOrder, long distanceMinute) {
    boolean isNullResellerOrder =
        resellerOrder == null || resellerOrder.getGmtPayingStart() == null;
    boolean isIn2min = distanceMinute <= 2;
    if (isNullResellerOrder && isIn2min) {
      return false;
    }
    boolean isNotIn2min = distanceMinute > 2;
    if (isNullResellerOrder && isNotIn2min) {
      return true;
    }
    return DateUtil.getDistanceMinute(resellerOrder.getGmtPayingStart(), new Date())
        > sysConfigService.getDBApplicationConfig().getOrderMaxExpireMin();
  }


  private void doSuccess(ResellerOrder resellerOrder, PretenderOrder pretenderOrder,
      PayOrder payOrder) {
    //update the resellerOrder,reset the status
    Date now = new Date();
    if (resellerOrder != null) {
      ResellerOrder updateResellerParam = new ResellerOrder();
      updateResellerParam.setMatchOutTradeNo(payOrder.getPayOrderId());
      updateResellerParam.setOrderStatus(ResellerOrderStatusEnum.FINISH.getCode());
      updateResellerParam.setGmtUpdate(now);
      updateResellerParam.setVersion(resellerOrder.getVersion());
      updateResellerParam.setId(resellerOrder.getId());
      resellerOrderService.updateById(updateResellerParam);
    }
    if (pretenderOrder != null) {
      PretenderOrder updatePretenderOrderParam = new PretenderOrder();
      updatePretenderOrderParam.setStatus(PretenderOrderStatusEnum.FINISH.getCode());
      updatePretenderOrderParam.setGmtUpdate(now);
      updatePretenderOrderParam.setGmtNotify(now);
      updatePretenderOrderParam.setVersion(pretenderOrder.getVersion());
      updatePretenderOrderParam.setId(pretenderOrder.getId());
      pretenderOrderService.updateById(updatePretenderOrderParam);
    }
  }

  protected void doExpireOperation(ResellerOrder resellerOrder, PretenderOrder pretenderOrder) {
    //update the resellerOrder,reset the status
    Date now = new Date();
    if (resellerOrder != null) {
      ResellerOrder updateResellerParam = new ResellerOrder();
      updateResellerParam.setVersion(resellerOrder.getVersion());
      updateResellerParam.setOrderStatus(ResellerOrderStatusEnum.SLEEP.getCode());
      updateResellerParam.setGmtPayingStart(null);
      updateResellerParam.setMatchOutTradeNo(null);
      updateResellerParam.setGmtUpdate(now);
      updateResellerParam.setId(resellerOrder.getId());
      resellerOrderService.updateById(updateResellerParam);
    }
    if (pretenderOrder != null) {
      PretenderOrder updatePretenderOrderParam = new PretenderOrder();
      updatePretenderOrderParam.setStatus(PretenderOrderStatusEnum.OVER_TIME.getCode());
      updatePretenderOrderParam.setGmtUpdate(now);
      updatePretenderOrderParam.setGmtExpired(now);
      updatePretenderOrderParam.setVersion(pretenderOrder.getVersion());
      updatePretenderOrderParam.setPretenderAccountId(pretenderOrder.getId());
      pretenderOrderService.updateById(updatePretenderOrderParam);
    }
  }


}
