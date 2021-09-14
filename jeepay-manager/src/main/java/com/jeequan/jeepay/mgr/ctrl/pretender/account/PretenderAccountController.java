package com.jeequan.jeepay.mgr.ctrl.pretender.account;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.oss.model.OssFileConfig;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PretenderAccount;
import com.jeequan.jeepay.core.entity.PretenderOrder;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.FileKit;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.PretenderAccountImportService;
import com.jeequan.jeepay.service.impl.PretenderAccountService;
import com.jeequan.jeepay.service.impl.PretenderOrderService;
import com.jeequan.jeepay.service.rq.PretenderAccountImportRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author axl rose
 * @date 2021/9/15
 */
@RestController
@RequestMapping("/api/pretenderAccounts")
@Slf4j
public class PretenderAccountController extends CommonCtrl {

    @Autowired private PretenderAccountService pretenderAccountService;

    @Autowired private PretenderAccountImportService pretenderAccountImportService;

    @Autowired private PretenderOrderService pretenderOrderService;

    /** 导入 */
    @PostMapping("/{bizType}/")
    public ApiRes batchUpload(@RequestParam("file") MultipartFile file, @PathVariable("bizType") String bizType) {

        if( file == null ) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "选择文件不存在");
        }
        try {

            OssFileConfig ossFileConfig = OssFileConfig.getOssFileConfigByBizType(bizType);

            //1. 判断bizType 是否可用
            if(ossFileConfig == null){
                throw new BizException("类型有误");
            }

            // 2. 判断文件是否支持
            String fileSuffix = FileKit.getFileSuffix(file.getOriginalFilename(), false);
            if( !ossFileConfig.isAllowFileSuffix(fileSuffix) ){
                throw new BizException("上传文件格式不支持！");
            }

            // 3. 判断文件大小是否超限
            if( !ossFileConfig.isMaxSizeLimit(file.getSize()) ){
                throw new BizException("上传大小请限制在["+ossFileConfig.getMaxSize() / 1024 / 1024 +"M]以内！");
            }
            PretenderAccountImportRequest pretenderAccountImportRequest = new PretenderAccountImportRequest();
            pretenderAccountImportRequest.setBizType(bizType);
            pretenderAccountImportRequest.setMultipartFile(file);
            pretenderAccountImportService.batchImport(pretenderAccountImportRequest);
            return ApiRes.ok();
        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            logger.error("upload error, fileName = {}", file.getOriginalFilename(), e);
            throw new BizException(ApiCodeEnum.SYSTEM_ERROR);
        }
    }

    @PreAuthorize("hasAnyAuthority('ENT_PC_WAY_LIST', 'ENT_PAY_ORDER_SEARCH_PAY_WAY')")
    @GetMapping
    public ApiRes list() {

        PretenderAccount pretenderAccount = getObject(PretenderAccount.class);

        LambdaQueryWrapper<PretenderAccount> condition = PretenderAccount.gw();
        if(StringUtils.isNotEmpty(pretenderAccount.getAccount())){
            condition.like(PretenderAccount::getAccount, pretenderAccount.getAccount());
        }
        if(StringUtils.isNotBlank(pretenderAccount.getBizType())){
            condition.like(PretenderAccount::getBizType, pretenderAccount.getBizType());
        }
        if(StringUtils.isNotBlank(pretenderAccount.getCertificate())){
            condition.like(PretenderAccount::getCertificate, pretenderAccount.getCertificate());
        }
        if(pretenderAccount.getId()==null){
            condition.eq(PretenderAccount::getId, pretenderAccount.getId());
        }

        JSONObject paramJSON = getReqParamJSON();
        if (paramJSON != null) {
            if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
                condition.ge(PretenderAccount::getGmtCreate, paramJSON.getString("createdStart"));
            }
            if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
                condition.le(PretenderAccount::getGmtCreate, paramJSON.getString("createdEnd"));
            }
        }
        condition.orderByDesc(PretenderAccount::getGmtCreate);
        IPage<ResellerOrder> pages = pretenderAccountService.page(getIPage(true), condition);
        return ApiRes.page(pages);
    }

    @PreAuthorize("hasAuthority('ENT_PAY_ORDER_VIEW')")
    @RequestMapping(value="/{pretenderAccountId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("pretenderAccountId") Long pretenderAccountId) {
        PretenderAccount pretenderAccount =  pretenderAccountService.getById(pretenderAccountId);
        if (pretenderAccount == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
        }
        return ApiRes.ok(pretenderAccount);
    }

    @PreAuthorize("hasAuthority('ENT_PC_WAY_EDIT')")
    @PutMapping("/{pretenderAccountId}")
    @MethodLog(remark = "更新伪装账号")
    public ApiRes update(@PathVariable("pretenderAccountId") Long pretenderAccountId) {
        PretenderAccount pretenderAccount = getObject(PretenderAccount.class);
        pretenderAccount.setId(pretenderAccountId);
        boolean result =pretenderAccountService.updateById(pretenderAccount);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }
        return ApiRes.ok();
    }

    @PreAuthorize("hasAuthority('ENT_PC_WAY_DEL')")
    @DeleteMapping("/{pretenderAccountId}")
    @MethodLog(remark = "删除支付方式")
    public ApiRes delete(@PathVariable("pretenderAccountId") Long pretenderAccountId) {
        int count =pretenderOrderService.count(PretenderOrder.gw()
                .eq(PretenderOrder::getPretenderAccountId,pretenderAccountId));
        if (count>0) {
            throw new BizException("该小号存在使用记录");
        }
        boolean result = pretenderOrderService.removeById(pretenderAccountId);
        if (!result) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_DELETE);
        }
        return ApiRes.ok();
    }

}
