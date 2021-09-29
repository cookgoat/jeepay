package com.jeequan.jeepay.mgr.ctrl.vo;

import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ResellerProductVo {
  /**
   * 产品类型
   */
  private String productType;

  /**
   * 产品名
   */
  private String productName;

  /**
   * 核销商号
   */
  private String resellerNo;


  /**
   * 信用金,单位分
   */
  private Long creditAmount=0L;

  /**
   * 产品核销费率，单位百分比，例如12.3%
   */
  private BigDecimal feeRate=BigDecimal.ZERO;

  /**
   * 状态 enable启用|disable禁用
   */
  private String status = SwitchStatusEnum.DISABLE.getCode();

}
