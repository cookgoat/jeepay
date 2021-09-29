package com.jeequan.jeepay.service.biz.fileentity;

import lombok.Data;
import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Data
public class ResellerOrderBaseExcelFileEntity {
    @Excel(name="订单号",orderNum = "0")
    private String orderNo;
    @Excel(name="充值账号",orderNum = "1")
    private String chargeAccount;
    @Excel(name="充值金额",orderNum = "2")
    private String amount;
    @Excel(name="所属运营商标志",orderNum = "3")
    private String queryFlag;
}
