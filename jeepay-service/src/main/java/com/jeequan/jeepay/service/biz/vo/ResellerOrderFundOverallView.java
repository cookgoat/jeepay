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
   private Long allOrderAmount;
   private Long allWaitAmount;
   private Long allFinishAmount;
   private Long allReturnedAmount;
   private Long allPayAmount;
   private Long allSleepAmount;
}
