package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 核销上订单，提供可供上游支付
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_reseller_order")
public class ResellerOrder implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 核销商id
     */
    private Long resellerId;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 充值账号
     */
    private String chargeAccount;

    /**
     * 充值金额，单位分
     */
    private Long amount;

    /**
     * 充值账号区域，存在区域维度，可为空
     */
    private String area;

    /**
     * 充值账号类型，分为手机号以及上游app三方账号
     */
    private String chargeAccountType;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 匹配的上游订单
     */
    private String matchOutTradeNo;

    /**
     * 匹配时间
     */
    private Date gmtMatch;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtUpdate;


}
