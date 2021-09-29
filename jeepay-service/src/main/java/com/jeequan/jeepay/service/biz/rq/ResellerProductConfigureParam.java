package com.jeequan.jeepay.service.biz.rq;

import lombok.Data;

@Data
public class ResellerProductConfigureParam {

  private String resellerNo;

  private String productType;

  private String status;

  private Double feeRate;

  private Long creditAmount;

  private Long currentUid;

}
