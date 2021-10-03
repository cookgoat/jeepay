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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.PropertyCreditUtil;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rq.QueryOrderRequest;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.service.biz.ResellerFundLineRecorder;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import com.jeequan.jeepay.pay.pretender.channel.propertycredit.kits.rs.QueryOrderResult;
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
  public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext)
      throws Exception {
    ResellerOrder resellerOrder = resellerOrderService
        .getOne(ResellerOrder.gw().eq(ResellerOrder::getMatchOutTradeNo, payOrder.getPayOrderId())
            .eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.PAYING), true);
    PretenderOrder pretenderOrder = pretenderOrderService
        .getOne(PretenderOrder.gw().eq(PretenderOrder::getOutTradeNo, payOrder.getChannelOrderNo())
            .eq(PretenderOrder::getStatus, PretenderOrderStatusEnum.PAYING), true);
    if(pretenderOrder==null){
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
    if (StringUtils.equalsIgnoreCase(notifyStatus, queryOrderResult.getData().getStatus())||
        queryOrderResult.getData().isSuccess()  ) {
      doSuccess(resellerOrder, pretenderOrder, payOrder);
      //todo use mq or thread
      taskExec.execute(() -> resellerFundLineRecorder.record(pretenderOrder));
      return ChannelRetMsg.confirmSuccess(queryOrderResult.getData().getOrderNo());
    }
    if (queryOrderResult.getData().isWaiting()) {
      return ChannelRetMsg.waiting();
    }
    if (isExpire(resellerOrder)) {
      doExpireOperation(resellerOrder, pretenderOrder);
      return ChannelRetMsg.confirmFail();
    }
    return ChannelRetMsg.unknown();
  }


  private boolean isExpire(ResellerOrder resellerOrder) {
    if (resellerOrder == null||resellerOrder.getGmtPayingStart()==null) {
      return true;
    }
    return DateUtil.getDistanceMinute(resellerOrder.getGmtPayingStart(), new Date())
        > sysConfigService.getDBApplicationConfig().getOrderMaxExpireMin();
  }


  private void doSuccess(ResellerOrder resellerOrder, PretenderOrder pretenderOrder,
      PayOrder payOrder) {
    //update the resellerOrder,reset the status
    if(resellerOrder!=null){
      resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>()
          .set(ResellerOrder::getMatchOutTradeNo, payOrder.getPayOrderId())
          .set(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.FINISH.getCode())
          .set(ResellerOrder::getGmtUpdate, new Date())
          .eq(ResellerOrder::getId, resellerOrder.getId()));
    }
    if(pretenderOrder!=null){
      pretenderOrderService.update((new LambdaUpdateWrapper<PretenderOrder>()
          .set(PretenderOrder::getStatus, PretenderOrderStatusEnum.FINISH)
          .set(PretenderOrder::getGmtUpdate, new Date()))
          .set(PretenderOrder::getGmtNotify, new Date())
          .eq(PretenderOrder::getId, pretenderOrder.getId()));
      pretenderOrder.setStatus(PretenderOrderStatusEnum.FINISH.getCode());
    }
  }

  protected void doExpireOperation(ResellerOrder resellerOrder, PretenderOrder pretenderOrder) {
    //update the resellerOrder,reset the status
    Date now = new Date();
    if (resellerOrder != null) {
      resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>()
          .set(ResellerOrder::getMatchOutTradeNo, null)
          .set(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.WAITING_PAY.getCode())
          .set(ResellerOrder::getGmtPayingStart, null)
          .set(ResellerOrder::getGmtUpdate, now)
          .eq(ResellerOrder::getId, resellerOrder.getId()));
    }
    pretenderOrderService.update((new LambdaUpdateWrapper<PretenderOrder>()
        .set(PretenderOrder::getStatus, PretenderOrderStatusEnum.OVER_TIME)
        .set(PretenderOrder::getGmtUpdate, now)
        .set(PretenderOrder::getGmtExpired, now))
        .eq(PretenderOrder::getId, pretenderOrder.getId()));
  }

}
