package com.jeequan.jeepay.service.biz;

import com.jeequan.jeepay.core.entity.PretenderOrder;

public interface PretenderAccountUseStatisticsRecorder {
  void recorder(PretenderOrder pretenderOrder);
}
