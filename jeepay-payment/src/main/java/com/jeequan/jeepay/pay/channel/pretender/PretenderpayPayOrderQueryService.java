package com.jeequan.jeepay.pay.channel.pretender;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.DateUtil;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.pretender.cs.PretenderOrderStatusEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.PropertyCreditUtil;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rq.QueryOrderRequest;
import com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs.QueryOrderResult;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    private static final long MAX_PAY_MINUTES = 10;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.PRETENDERPAY;
    }

    @Override
    public ChannelRetMsg query(PayOrder payOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        ResellerOrder resellerOrder = resellerOrderService.getOne(ResellerOrder.gw().eq(ResellerOrder::getMatchOutTradeNo, payOrder.getMchOrderNo())
                .eq(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.PAYING), false);
        PretenderOrder pretenderOrder = pretenderOrderService.getOne(PretenderOrder.gw().eq(PretenderOrder::getOutTradeNo, payOrder.getChannelOrderNo())
                .eq(PretenderOrder::getStatus, PretenderOrderStatusEnum.PAYING), false);
        QueryOrderRequest queryOrderRequest = new QueryOrderRequest();
        queryOrderRequest.setOrderNo(pretenderOrder.getOutTradeNo());
        PretenderAccount pretenderAccount = pretenderAccountService.getOne(PretenderAccount.gw().eq(PretenderAccount::getId, pretenderOrder.getPretenderAccountId()), false);
        queryOrderRequest.setCookie(pretenderAccount.getCertificate());
        QueryOrderResult queryOrderResult = PropertyCreditUtil.queryOrder(queryOrderRequest);
        if (!queryOrderResult.isSuccess() || queryOrderResult.getData() == null) {
            throw new BizException(QUERY_PRETENDER_ORDER_FAILED);
        }
        if (queryOrderResult.getData().isSuccess()) {
            doSuccess(resellerOrder, pretenderOrder, payOrder);
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
        return DateUtil.getDistanceMinute(resellerOrder.getGmtPayingStart(), new Date()) > MAX_PAY_MINUTES;
    }


    private void doSuccess(ResellerOrder resellerOrder, PretenderOrder pretenderOrder, PayOrder payOrder) {
        //update the resellerOrder,reset the status
        resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>()
                .set(ResellerOrder::getMatchOutTradeNo, payOrder.getMchOrderNo())
                .set(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.FINISH.getCode())
                .set(ResellerOrder::getGmtUpdate, new Date())
                .eq(ResellerOrder::getId, resellerOrder.getId()));

        pretenderOrderService.update((new LambdaUpdateWrapper<PretenderOrder>()
                .set(PretenderOrder::getStatus, PretenderOrderStatusEnum.FINISH)
                .set(PretenderOrder::getGmtUpdate, new Date()))
                .set(PretenderOrder::getGmtNotify, new Date())
                .eq(PretenderOrder::getId, pretenderOrder.getId()));
    }


    protected void doExpireOperation(ResellerOrder resellerOrder, PretenderOrder pretenderOrder) {
        //update the resellerOrder,reset the status
        Date now = new Date();
        resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>()
                .set(ResellerOrder::getMatchOutTradeNo, null)
                .set(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.WAITING_PAY.getCode())
                .set(ResellerOrder::getGmtPayingStart, null)
                .set(ResellerOrder::getGmtUpdate, now)
                .eq(ResellerOrder::getId, resellerOrder.getId()));

        pretenderOrderService.update((new LambdaUpdateWrapper<PretenderOrder>()
                .set(PretenderOrder::getStatus, PretenderOrderStatusEnum.OVER_TIME)
                .set(PretenderOrder::getGmtUpdate, now)
                .set(PretenderOrder::getGmtExpired, now))
                .eq(PretenderOrder::getId, pretenderOrder.getId()));
    }

}
