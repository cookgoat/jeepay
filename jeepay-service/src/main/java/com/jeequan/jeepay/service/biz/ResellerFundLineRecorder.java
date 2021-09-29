package com.jeequan.jeepay.service.biz;

import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PretenderOrder;

public interface ResellerFundLineRecorder {
  void  record(PretenderOrder pretenderOrder);
}
