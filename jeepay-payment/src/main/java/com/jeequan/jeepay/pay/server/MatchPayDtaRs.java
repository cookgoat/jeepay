package com.jeequan.jeepay.pay.server;

import lombok.Data;
import java.util.Date;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;

@Data
public class MatchPayDtaRs {
  private String mchOrderNo;
  private String amount;
  private String payType;
  private Date matchEndTime;
  private String payUrl;
  private String code;
  /** 上游渠道返回数据包 (无需JSON序列化) **/
  @JSONField(serialize = false)
  private ChannelRetMsg channelRetMsg;
}
