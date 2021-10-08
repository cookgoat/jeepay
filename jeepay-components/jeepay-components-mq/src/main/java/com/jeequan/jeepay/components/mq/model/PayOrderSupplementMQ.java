package com.jeequan.jeepay.components.mq.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.components.mq.constant.MQSendTypeEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderSupplementMQ extends  AbstractMQ{
  /** 【！重要配置项！】 定义MQ名称 **/
  public static final String MQ_NAME = "QUEUE_PAY_ORDER_SUPPLEMENT_MQ";

  /** 内置msg 消息体定义 **/
  private PayOrderSupplementMQ.MsgPayload payload;

  /**  【！重要配置项！】 定义Msg消息载体 **/
  @Data
  @AllArgsConstructor
  public static class MsgPayload {

    /** 支付订单号 **/
    private String payOrderId;

    /** 通知次数 **/
    private Integer count;

  }

  @Override
  public String getMQName() {
    return MQ_NAME;
  }

  /**  【！重要配置项！】 **/
  @Override
  public MQSendTypeEnum getMQType(){
    return MQSendTypeEnum.QUEUE;  // QUEUE - 点对点 、 BROADCAST - 广播模式
  }

  @Override
  public String toMessage() {
    return JSONObject.toJSONString(payload);
  }

  /**  【！重要配置项！】 构造MQModel , 一般用于发送MQ时 **/
  public static PayOrderSupplementMQ build(String payOrderId, Integer count){
    return new PayOrderSupplementMQ(new PayOrderSupplementMQ.MsgPayload(payOrderId, count));
  }

  /** 解析MQ消息， 一般用于接收MQ消息时 **/
  public static PayOrderSupplementMQ.MsgPayload parse(String msg){
    return JSON.parseObject(msg, PayOrderSupplementMQ.MsgPayload.class);
  }

  /** 定义 IMQReceiver 接口： 项目实现该接口则可接收到对应的业务消息  **/
  public interface IMQReceiver{
    void receive(PayOrderSupplementMQ.MsgPayload payload);
  }
}
