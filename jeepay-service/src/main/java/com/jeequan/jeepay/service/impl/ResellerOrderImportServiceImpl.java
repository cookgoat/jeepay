package com.jeequan.jeepay.service.impl;

import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderChargeAccountType;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.ExcelUtil;
import com.jeequan.jeepay.core.utils.SnowflakeIdWorker;
import com.jeequan.jeepay.service.ResellerOrderImportService;
import com.jeequan.jeepay.service.fileentity.ResellerOrderBaseExcelFileEntity;
import com.jeequan.jeepay.service.rq.ResellerOrderImportRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.jeequan.jeepay.core.constants.ProductTypeEnum.isRightProductType;
import static com.jeequan.jeepay.core.utils.ExcelUtil.isExcelFile;
import static com.jeequan.jeepay.core.utils.RegexUtil.isInteger;
import static com.jeequan.jeepay.core.utils.RegexUtil.isMobile;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Service
public class ResellerOrderImportServiceImpl implements ResellerOrderImportService {

    @Autowired
    private ResellerOrderService resellerOrderService;

    private static final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(1, 1);

    @Override
    public void batchImport(ResellerOrderImportRequest resellerImportRequest) {
        try {
            checkParam(resellerImportRequest);
            List<ResellerOrderBaseExcelFileEntity> resellerOrderBaseExcelFileEntities =
                    ExcelUtil.importExcel(resellerImportRequest.getMultipartFile(), 0, 1, ResellerOrderBaseExcelFileEntity.class);
            List<ResellerOrder> resellerOrderList = buildResellerOrderList(resellerImportRequest, resellerOrderBaseExcelFileEntities);
            if(resellerOrderList==null||resellerOrderList.size()<=0){
                return;
            }
            boolean isSuccess= resellerOrderService.saveBatch(resellerOrderList);
            if(!isSuccess){
                throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ResellerOrder> buildResellerOrderList(ResellerOrderImportRequest resellerImportRequest, List<ResellerOrderBaseExcelFileEntity> resellerOrderBaseExcelFileEntities) {
        List<ResellerOrder> resellerOrderList = new ArrayList<>();
        if (resellerOrderBaseExcelFileEntities != null && resellerOrderBaseExcelFileEntities.size() > 0) {
            for (ResellerOrderBaseExcelFileEntity resellerOrderBaseExcelFileEntity : resellerOrderBaseExcelFileEntities) {
                ResellerOrder resellerOrder = convert(resellerImportRequest, resellerOrderBaseExcelFileEntity);
                if (resellerOrder == null) {
                    continue;
                }
                resellerOrderList.add(resellerOrder);
            }
        }
        return resellerOrderList;
    }

    private ResellerOrder convert(ResellerOrderImportRequest resellerImportRequest, ResellerOrderBaseExcelFileEntity resellerOrderBaseExcelFileEntity) {
        ResellerOrder resellerOrder = new ResellerOrder();
        if (resellerOrderBaseExcelFileEntity.getResellerId() == null) {
            resellerOrder.setResellerId(resellerImportRequest.getCurrentUserId());
        } else {
            resellerOrder.setResellerId(resellerOrderBaseExcelFileEntity.getResellerId());
        }
        if (StringUtils.isBlank(resellerOrderBaseExcelFileEntity.getOrderNo())) {
            resellerOrder.setOrderNo(generateOrderNo());
        } else {
            resellerOrder.setOrderNo(resellerOrderBaseExcelFileEntity.getOrderNo());
        }
        if (StringUtils.isBlank(resellerOrderBaseExcelFileEntity.getAmount()) || !isInteger(resellerOrderBaseExcelFileEntity.getAmount())) {
            return null;
        }
        resellerOrder.setAmount(Long.valueOf(AmountUtil.convertDollar2Cent(resellerOrderBaseExcelFileEntity.getAmount())));

        if (StringUtils.isBlank(resellerOrderBaseExcelFileEntity.getChargeAccount())) {
            return null;
        }
        resellerOrder.setChargeAccount(resellerOrderBaseExcelFileEntity.getChargeAccount());
        resellerOrder.setQueryFlag(resellerOrderBaseExcelFileEntity.getQueryFlag());
        resellerOrder.setProductType(resellerImportRequest.getProductType());
        resellerOrder.setGmtCreate(new Date());
        if (isMobile(resellerOrderBaseExcelFileEntity.getChargeAccount())) {
            resellerOrder.setChargeAccountType(ResellerOrderChargeAccountType.MOBILE.getCode());
        } else {
            resellerOrder.setChargeAccountType(ResellerOrderChargeAccountType.PLATFORM_ACCOUNT.getCode());
        }
        resellerOrder.setOrderStatus(ResellerOrderStatusEnum.WAITING_PAY.getCode());
        return resellerOrder;
    }

    private String generateOrderNo() {
        return "R" + snowflakeIdWorker.nextId();
    }



    private void checkParam(ResellerOrderImportRequest resellerOrderImportRequest) throws IOException {
        if (resellerOrderImportRequest == null ||
                StringUtils.isBlank(resellerOrderImportRequest.getProductType()) ||
                resellerOrderImportRequest.getCurrentUserId() == 0 ||
                resellerOrderImportRequest.getMultipartFile() == null ||
                !isExcelFile(resellerOrderImportRequest.getMultipartFile().getInputStream()) ||
                !isRightProductType(resellerOrderImportRequest.getProductType())) {
            throw new BizException(ApiCodeEnum.PARAMS_ERROR);
        }
    }


}
