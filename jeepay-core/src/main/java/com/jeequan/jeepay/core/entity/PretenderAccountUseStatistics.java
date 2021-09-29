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
 * 
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-09-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_pretender_account_use_statistics")
public class PretenderAccountUseStatistics implements Serializable {

    private static final long serialVersionUID=1L;

    public static final LambdaQueryWrapper<PretenderAccountUseStatistics> gw(){
        return new LambdaQueryWrapper<>();
    }

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 伪装账号id
     */
    private Long pretenderAccountId;

    /**
     * 成功次数
     */
    private Long successCount = 0L;

    /**
     * 失败次数
     */
    private Long failedCount = 0L;

    /**
     * 总共请求次数
     */
    private Long allRequestCount = 0L;

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
     * 最近一次失败时间
     */
    private Date gmtLastFailed;

    /**
     * 最近一次成功时间
     */
    private Date gmtLastSuccess;

    /**
     * 最近一次请求时间
     */
    private Date gmtLast;


}
