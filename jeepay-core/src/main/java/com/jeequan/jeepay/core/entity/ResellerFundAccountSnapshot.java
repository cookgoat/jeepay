package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.Date;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * <p>
 * 核销商当日资金账户快照表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_reseller_fund_account_snapshot")
public class ResellerFundAccountSnapshot implements Serializable {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<ResellerFundAccountSnapshot> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 核销商号
     */
    private String resellerNo;

    /**
     * 核销商所有订单金额，导入的时候进行增加,单位分
     */
    private Long allAmount;

    /**
     * 回款金额
     */
    private Long recoveriesAllAmount;

    /**
     * 所有完成订单总金额，支付订单做加减或者直接统计，单位分
     */
    private Long finishedAllAmount;

    /**
     * 所有待充值总金额，支付订单时做加减或者直接统计，单位分
     */
    private Long waitAllAmount;

    /**
     * 核销商所有分享金额
     */
    private Long shareAllAmount;

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
