package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 核销商资金流水
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_reseller_fund_line")
public class ResellerFundLine implements Serializable {

    private static final long serialVersionUID=1L;


    public static final LambdaQueryWrapper<ResellerFundLine> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 核销商户号
     */
    private String resellerNo;

    /**
     * 变更前回款金额
     */
    private Long beforeRecoveriesAmount;

    /**
     * 变更金额
     */
    private Long changeRecoveriesAmount;

    /**
     * 变更后回款金额
     */
    private Long afterRecoveriesAmount;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 产品订单
     */
    private String productOrderNo;

    /**
     * 订单金额
     */
    private Long orderAmount;

    /**
     * 手续费
     */
    private Long resellerShareAmount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;


}
