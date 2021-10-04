package com.jeequan.jeepay.service.biz.fileentity;

import lombok.Data;
import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Data
public class ResellerOrderBaseExcelFileEntity {

  @Excel(name = "充值账号", orderNum = "0")
  private String chargeAccount;

  @Excel(name = "充值金额", orderNum = "1")
  private String amount;

  @Excel(name = "子核销简称", orderNum = "2")
  private String queryFlag;
}
