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
 * 核销商，为支付平台下游支付渠道提供真实的支付订单
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_reseller")
public class Reseller extends BaseModel implements Serializable {

    private static final long serialVersionUID=1L;


    public static final LambdaQueryWrapper<Reseller> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 核销商名称
     */
    private String resellerName;

    /**
     * 核销商户号
     */
    private String resellerNo;

    /**
     * 核销商状态，enable,disable
     */
    private String status;

    /**
     * 创建核销商时，允许他登录的账户id
     */
    private Long initUserId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者uid
     */
    private Long createUid;

    /**
     * 创建者用户名
     */
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    /**
     * 最后更新人uid
     */
    private Long lastUpdateUid;

    /**
     * 最后更新人姓名
     */
    private String lastUpdateName;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtUpdate;

    /**
     * 联系人手机号
     */
    private String contactTel;

}
