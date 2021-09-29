package com.jeequan.jeepay.core.model.params.reseller;

import lombok.Data;


@Data
public class ResellerFundLineParams {

    /**产品类型*/
    private String productType;
    /**产品订单号*/
    private String productOrderNo;
    /**核销商户号*/
    private String resellerNo;
    /**金额起始*/
    private Integer amountStart;
    /**金额起止*/
    private Integer amountEnd;
    /**创建时间开始*/
    private String startDate;
    /**创建时间结束*/
    private String endDate;
}
