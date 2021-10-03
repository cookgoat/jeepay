package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.util.Date;
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
 * 伪装订单，使用伪装账号生成的上游订单
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pretender_order")
public class PretenderOrder implements Serializable {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<PretenderOrder> gw(){
        return new LambdaQueryWrapper<>();
    }


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型，与伪装小号、核销订单业务类型保持一致
     */
    private String bizType;

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 状态
     */
    private String status;

    /**
     * 伪装小号账号id
     */
    private Long pretenderAccountId;

    /**
     * 充值金额，单位，分
     */
    private Long amount;

    /**
     * 支付方式
     */
    private String payWay;

    /**
     * 生成的支付链接
     */
    private String payUrl;

    /**
     * 回调时间
     */
    private Date gmtNotify;

    /**
     * 匹配的核销商订单
     */
    private String matchResellerOrderNo;

    /**
     * 扩展字段，存储json
     */
    private String ext;

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

    /**
     * 产品类型
     */
    private String productType;

    /**
     * 过期时间
     */
   private  Date gmtExpired;

    /**
     * 版本
     */
    @Version
    private Integer version;
}
