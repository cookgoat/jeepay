package com.jeequan.jeepay.service.biz.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author axl rose
 * @date 2021/10/4
 */

@Data
public class ResellerOrderCountVo {
  @Excel(name = "核销商名称", orderNum = "0")
  private String resellerName;
  @Excel(name = "核销商id", orderNum = "1")
  private String resellerNo;
  @Excel(name = "业务类型", orderNum = "2")
  private String productType;
  @Excel(name = "订单状态", orderNum = "3")
  private String orderStatus;
  @Excel(name = "充值账号", orderNum = "4")
  private String chargeAccount;
  @Excel(name = "订单总金额", orderNum = "5")
  private Long orderAllAmount;
  @Excel(name = "订单总完成金额", orderNum = "6")
  private Long finishAllAmount;
  @Excel(name="子核销商",orderNum = "7")
  private String queryFlag;
}
