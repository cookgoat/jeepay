package com.jeequan.jeepay.service.biz.vo;

import lombok.Data;

/**
 * @author axl rose
 * @date 2021/10/5
 */
@Data
public class ResellerOrderFundOverallView {
   private String resellerNo;
   private String resellerName;
   private Long allOrderAmount=0L;
   private Long allWaitAmount=0L;
   private Long allFinishAmount=0L;
   private Long allReturnedAmount=0L;
   private Long allPayAmount=0L;
   private Long allSleepAmount=0L;
}
