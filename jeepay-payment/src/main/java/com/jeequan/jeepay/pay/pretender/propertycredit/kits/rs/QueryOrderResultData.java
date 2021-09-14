package com.jeequan.jeepay.pay.pretender.propertycredit.kits.rs;

import com.jeequan.jeepay.pay.pretender.propertycredit.kits.CS;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author axl rose
 * @date 2021/9/14
 */

@Data
public class QueryOrderResultData {
    private String id;
    private String merchantId;
    private String orderNo;
    private String rechargeProduct;
    private String rechargeAccount;
    private String rechargeType;
    private Long rechargeAmount;
    private String totalQuantity;
    private Date createTime;
    private String status;
    private String isOpenInvoice;
    private String payChannel;
    private String userNo;
    private Long payAmount;
    private String payOrderNo;
    private String topupMerchantId;
    public boolean isSuccess(){
        return StringUtils.equalsAnyIgnoreCase(this.status, CS.PROPERTY_CREDIT_API_RESULT_CODE.SUCCESS_CODE);
    }

    public boolean isWaiting(){
        return StringUtils.equalsAnyIgnoreCase(this.status, CS.PROPERTY_CREDIT_API_RESULT_CODE.WAIT_CODE);
    }

}
