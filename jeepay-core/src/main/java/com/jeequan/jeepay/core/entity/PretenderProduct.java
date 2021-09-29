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
 * 支付渠道产品表，核销商订单、下游支付渠道关联的产品对象
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pretender_product")
public class PretenderProduct implements Serializable {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<PretenderProduct> gw(){
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
     * 产品名
     */
    private String productName;

    /**
     * 状态 enable启用|disable禁用
     */
    private String status;

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
     * 更新人id
     */
    private Long updateUid;


}
