package com.jeequan.jeepay.core.entity;

import lombok.Data;
import java.util.Date;
import java.math.BigDecimal;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * <p>
 * 核销商核销产品列表
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_reseller_pretender_product")
public class ResellerPretenderProduct implements Serializable {

    private static final long serialVersionUID=1L;


    public static final LambdaQueryWrapper<ResellerPretenderProduct> gw(){
        return new LambdaQueryWrapper<>();
    }


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 核销商号
     */
    private String resellerNo;


    /**
     * 信用金,单位分
     */
    private Long creditAmount;
    /**
     * 待汇款金额，单位分
     */
    private Long recoveriesAmount;

    /**
     * 状态 enable启用|disable禁用
     */
    private String status;

    /**
     * 产品核销费率，单位百分比，例如12.3%
     */
    private BigDecimal feeRate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    /**
     * 创建人id
     */
    private Long createUid;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtUpdate;

    /**
     * 更新人uid
     */
    private Long updateUid;


}
