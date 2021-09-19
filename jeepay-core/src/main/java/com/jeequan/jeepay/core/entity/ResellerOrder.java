package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.model.BaseModel;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
public class ResellerOrder extends BaseModel implements Serializable {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<ResellerOrder> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
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
    private String productType;

    /**
     * 充值账号
     */
    private String chargeAccount;

    /**
     * 充值金额，单位分
     */
    private Long amount;

    /**
     * 查询标志，不同业务类型的订单有不同的查询标志
     */
    private String queryFlag;

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
    private Date gmtPayingStart;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtUpdate;


}
