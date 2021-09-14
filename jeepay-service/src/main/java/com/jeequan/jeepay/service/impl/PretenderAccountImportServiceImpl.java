package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.PretenderAccountStatusEnum;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.ExcelUtil;
import com.jeequan.jeepay.service.PretenderAccountImportService;
import com.jeequan.jeepay.service.fileentity.PretenderAccountExcelFileEntity;
import com.jeequan.jeepay.service.rq.PretenderAccountImportRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jeequan.jeepay.core.constants.BizTypeEnum.isRightBizType;
import static com.jeequan.jeepay.core.utils.ExcelUtil.isExcelFile;

/**
 * @author axl rose
 * @date 2021/9/14
 */

@Service
public class PretenderAccountImportServiceImpl implements PretenderAccountImportService {

    @Autowired
    private PretenderAccountService pretenderAccountService;

    @Override
    public void batchImport(PretenderAccountImportRequest pretenderAccountImportRequest) {
        try {
            checkParam(pretenderAccountImportRequest);
            List<PretenderAccountExcelFileEntity> pretenderAccountExcelFileEntityList =
                    ExcelUtil.importExcel(pretenderAccountImportRequest.getMultipartFile(), 0, 1, PretenderAccountExcelFileEntity.class);
           List<PretenderAccount> pretenderAccountList = buildResellerOrderList(pretenderAccountImportRequest,pretenderAccountExcelFileEntityList);
           if(pretenderAccountList==null||pretenderAccountList.size()<=0){
               return;
           }
           boolean isSuccess= pretenderAccountService.saveBatch(pretenderAccountList);
           if(!isSuccess){
               throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkParam(PretenderAccountImportRequest pretenderAccountImportRequest) throws IOException {
        if (pretenderAccountImportRequest == null ||
                StringUtils.isBlank(pretenderAccountImportRequest.getBizType()) ||
                pretenderAccountImportRequest.getMultipartFile() == null ||
                !isExcelFile(pretenderAccountImportRequest.getMultipartFile().getInputStream()) ||
                !isRightBizType(pretenderAccountImportRequest.getBizType())) {
            throw new BizException(ApiCodeEnum.PARAMS_ERROR);
        }
    }


    private List<PretenderAccount> buildResellerOrderList(PretenderAccountImportRequest pretenderAccountImportRequest,
                                                          List<PretenderAccountExcelFileEntity> pretenderAccountExcelFileEntityList) {
        List<PretenderAccount> pretenderAccountList = new ArrayList<>(0);
        if (pretenderAccountExcelFileEntityList != null && pretenderAccountExcelFileEntityList.size() > 0) {
            for (PretenderAccountExcelFileEntity pretenderAccountExcelFileEntity : pretenderAccountExcelFileEntityList) {
                PretenderAccount pretenderAccount = convert(pretenderAccountImportRequest,pretenderAccountExcelFileEntity);
                if (pretenderAccount == null) {
                    continue;
                }
                pretenderAccountList.add(pretenderAccount);
            }
        }
        return pretenderAccountList;
    }

    private PretenderAccount  convert(PretenderAccountImportRequest pretenderAccountImportRequest,
                                      PretenderAccountExcelFileEntity pretenderAccountExcelFileEntity){
        if(StringUtils.isBlank(pretenderAccountExcelFileEntity.getCertificate())){
            return null;
        }
        PretenderAccount  pretenderAccount = new PretenderAccount();
        pretenderAccount.setAccount(pretenderAccountExcelFileEntity.getAccount());
        pretenderAccount.setCertificate(pretenderAccountExcelFileEntity.getCertificate());
        pretenderAccount.setBizType(pretenderAccountImportRequest.getBizType());
        Date now = new Date();
        pretenderAccount.setGmtCreate(now);
        pretenderAccount.setStatus(PretenderAccountStatusEnum.AVAILABLE.getCode());
        return pretenderAccount;
    }
}
