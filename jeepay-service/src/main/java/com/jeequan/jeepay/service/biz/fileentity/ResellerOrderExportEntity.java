package com.jeequan.jeepay.service.biz.fileentity;

import java.util.Date;
import lombok.Data;
import cn.afterturn.easypoi.excel.annotation.Excel;

/**
 * @author axl rose
 * @date 2021/10/2
 */
@Data
public class ResellerOrderExportEntity {
  @Excel(name="订单号",orderNum = "0")
  private String orderNo;
  @Excel(name="充值账号",orderNum = "1")
  private String chargeAccount;
  @Excel(name="充值金额",orderNum = "2")
  private String amount;
  @Excel(name="产品类型",orderNum = "3")
  private String productType;
  @Excel(name="订单状态",orderNum = "4")
  private String orderStatus;
  @Excel(name="核销商id",orderNum = "5")
  private String resellerNo;
  @Excel(name="创建时间",orderNum = "6")
  private String gmtCreate;
  @Excel(name="完成时间",orderNum = "7")
  private String gmtUpdate;
}
