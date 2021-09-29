package com.jeequan.jeepay.core.model.params.reseller;

import lombok.Data;

@Data
public class ResellerFundAccountParams {
    /**所属产品*/
    private String resellerNo;
    /**开始时间*/
    private String startDate;
    /**结束时间*/
    private String endDate;
    /**回款金额起始*/
    private Integer amountStart;
    /**回款金额起止*/
    private Integer amountEnd;
}
