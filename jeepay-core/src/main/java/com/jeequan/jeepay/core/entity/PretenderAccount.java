package com.jeequan.jeepay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 伪装账号，用于调取上游支付接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pretender_account")
public class PretenderAccount implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 伪装账号名
     */
    private String account;

    /**
     * 伪装账号业务类型，标名伪装账号用于上游业务场景
     */
    private String bizType;

    /**
     * 伪装账号登录产生的凭证，用于表明账号登录态
     */
    private String certificate;

    /**
     * 伪装账号状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtUpdate;

    /**
     * 扩展字段
     */
    private String ext;


}
