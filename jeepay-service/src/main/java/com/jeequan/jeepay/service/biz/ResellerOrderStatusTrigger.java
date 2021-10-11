package com.jeequan.jeepay.service.biz;

import com.jeequan.jeepay.service.biz.rq.ResellerOrderTriggerRequest;

public interface ResellerOrderStatusTrigger {

  void batchEnable(ResellerOrderTriggerRequest resellerOrderTriggerRequest);

  void batchDisable(ResellerOrderTriggerRequest resellerOrderTriggerRequest);

}
