package com.jeequan.jeepay.service.biz.impl;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.core.constants.PretenderOrderStatusEnum;
import com.jeequan.jeepay.core.entity.PretenderAccountUseStatistics;
import com.jeequan.jeepay.service.impl.PretenderAccountUseStatisticsService;
import com.jeequan.jeepay.service.biz.PretenderAccountUseStatisticsRecorder;

@Service
public class PretenderAccountUseStatisticsRecorderImpl implements
    PretenderAccountUseStatisticsRecorder {

  @Autowired
  private PretenderAccountUseStatisticsService pretenderAccountUseStatisticsService;

  @Override
  public void recorder(PretenderOrder pretenderOrder) {
    checkParam(pretenderOrder);
    PretenderAccountUseStatistics pretenderAccountUseStatistics = queryByPretenderAccountId(pretenderOrder.getPretenderAccountId());
    Date now = new Date();
    pretenderAccountUseStatistics.setGmtLast(now);
    pretenderAccountUseStatistics.setAllRequestCount(pretenderAccountUseStatistics.getAllRequestCount()+1);
    if(StringUtils.equalsIgnoreCase(pretenderOrder.getStatus(), PretenderOrderStatusEnum.PAYING.getCode())){
      pretenderAccountUseStatistics.setSuccessCount(pretenderAccountUseStatistics.getSuccessCount()+1);
      pretenderAccountUseStatistics.setGmtLastSuccess(now);
    }
    if(StringUtils.equalsIgnoreCase(pretenderOrder.getStatus(), PretenderOrderStatusEnum.REQUEST_FAILED.getCode())){
      pretenderAccountUseStatistics.setFailedCount(pretenderAccountUseStatistics.getFailedCount()+1);
      pretenderAccountUseStatistics.setGmtLastFailed(now);
    }
  }

  private void checkParam(PretenderOrder pretenderOrder) {
    if (pretenderOrder == null) {
    }
  }

  private PretenderAccountUseStatistics queryByPretenderAccountId(Long PretenderAccountId) {
    PretenderAccountUseStatistics pretenderAccountUseStatistics = pretenderAccountUseStatisticsService
        .getOne(PretenderAccountUseStatistics.gw()
            .eq(PretenderAccountUseStatistics::getPretenderAccountId, PretenderAccountId));
    if(pretenderAccountUseStatistics==null){
      pretenderAccountUseStatistics = new PretenderAccountUseStatistics();
      pretenderAccountUseStatistics.setPretenderAccountId(PretenderAccountId);
    }
    return pretenderAccountUseStatistics;
  }

}
