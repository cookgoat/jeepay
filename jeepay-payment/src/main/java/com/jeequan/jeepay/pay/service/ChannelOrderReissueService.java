/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.RefundOrder;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IPayOrderQueryService;
import com.jeequan.jeepay.pay.channel.IRefundService;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.service.impl.PayOrderService;
import com.jeequan.jeepay.service.impl.RefundOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
* 查询上游订单， &  补单服务实现类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:40
*/

@Service
@Slf4j
public class ChannelOrderReissueService {

    @Autowired private ConfigContextService configContextService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private PayMchNotifyService payMchNotifyService;


    /** 处理订单 **/
    public ChannelRetMsg processPayOrder(PayOrder payOrder){

        try {

            String payOrderId = payOrder.getPayOrderId();

            //查询支付接口是否存在
            IPayOrderQueryService queryService = SpringBeansUtil.getBean(payOrder.getIfCode() + "PayOrderQueryService", IPayOrderQueryService.class);

            // 支付通道接口实现不存在
            if(queryService == null){
                log.error("{} interface not exists!", payOrder.getIfCode());
                return null;
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextService.getMchAppConfigContext(payOrder.getMchNo(), payOrder.getAppId());

            ChannelRetMsg channelRetMsg = queryService.query(payOrder, mchAppConfigContext);
            if(channelRetMsg == null){
                log.error("channelRetMsg is null");
                return null;
            }

            log.info("补单[{}]查询结果为：{}", payOrderId, channelRetMsg);

            // 查询成功
            if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                if (payOrderService.updateIng2Success(payOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId())) {

                    //订单支付成功，其他业务逻辑
                    payOrderProcessService.confirmSuccess(payOrder);
                }
            }else if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL){  //确认失败

                //1. 更新支付订单表为失败状态
                payOrderService.updateIng2Fail(payOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
                //发送商户通知
                payMchNotifyService.payOrderNotify(payOrder);
            }else if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CANCEL){  //确认取消

                //1. 更新支付订单表为失败状态
                payOrderService.updateIng2Cancle(payOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId());

            }

            return channelRetMsg;

        } catch (Exception e) {  //继续下一次迭代查询
            log.error("error payOrderId = {}", payOrder.getPayOrderId(), e);
            return null;
        }

    }

    /** 处理退款订单 **/
    public ChannelRetMsg processRefundOrder(RefundOrder refundOrder){

        try {

            String refundOrderId = refundOrder.getRefundOrderId();

            //查询支付接口是否存在
            IRefundService queryService = SpringBeansUtil.getBean(refundOrder.getIfCode() + "RefundService", IRefundService.class);

            // 支付通道接口实现不存在
            if(queryService == null){
                log.error("退款补单：{} interface not exists!", refundOrder.getIfCode());
                return null;
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextService.getMchAppConfigContext(refundOrder.getMchNo(), refundOrder.getAppId());

            ChannelRetMsg channelRetMsg = queryService.query(refundOrder, mchAppConfigContext);
            if(channelRetMsg == null){
                log.error("退款补单：channelRetMsg is null");
                return null;
            }

            log.info("退款补单：[{}]查询结果为：{}", refundOrderId, channelRetMsg);

            // 查询成功
            if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                if (refundOrderService.updateIng2Success(refundOrderId, channelRetMsg.getChannelOrderId())) {

                    // 通知商户系统
                    if(StringUtils.isNotEmpty(refundOrder.getNotifyUrl())){
                        payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
                    }

                }
            }else if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL){  //确认失败

                //1. 更新支付订单表为失败状态
                refundOrderService.updateIng2Fail(refundOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());

                // 通知商户系统
                if(StringUtils.isNotEmpty(refundOrder.getNotifyUrl())){
                    payMchNotifyService.refundOrderNotify(refundOrderService.getById(refundOrderId));
                }

            }

            return channelRetMsg;

        } catch (Exception e) {  //继续下一次迭代查询
            log.error("退款补单：error refundOrderId = {}", refundOrder.getRefundOrderId(), e);
            return null;
        }

    }


}
