package com.jeequan.jeepay.service.fileentity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Data
public class PretenderAccountExcelFileEntity {
    @Excel(name="账号名",orderNum = "0")
    private String  account;
    @Excel(name="登录凭证",orderNum = "1")
    private  String certificate;
}
