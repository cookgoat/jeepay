package com.jeequan.jeepay.pay.service;

import com.alibaba.fastjson.JSONObject;

public interface ChannelOrderQuery {
    JSONObject queryByChannelOrderId(String channelOrderId);
}
