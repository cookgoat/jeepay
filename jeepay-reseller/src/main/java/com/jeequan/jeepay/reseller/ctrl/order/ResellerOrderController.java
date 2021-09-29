package com.jeequan.jeepay.reseller.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.oss.model.OssFileConfig;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.FileKit;
import com.jeequan.jeepay.reseller.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.biz.ResellerOrderImportService;
import com.jeequan.jeepay.service.biz.rq.ResellerOrderImportRequest;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@RestController
@RequestMapping("/api/resellerOrders")
@Slf4j
public class ResellerOrderController extends CommonCtrl {

  @Autowired
  private ResellerOrderImportService resellerOrderImportService;

  @Autowired
  private ResellerOrderService resellerOrderService;

  /**
   * 导入
   */
  @PreAuthorize("hasAuthority('ENT_RESELLER_ORDER_GROUP_IMPORT')")
  @PostMapping("/{productType}/")
  public ApiRes batchUpload(@RequestParam("file") MultipartFile file,
      @PathVariable("productType") String productType) {

    if (file == null) {
      return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "选择文件不存在");
    }
    try {

      OssFileConfig ossFileConfig = OssFileConfig.getOssFileConfigByBizType(productType);

      //1. 判断bizType 是否可用
      if (ossFileConfig == null) {
        throw new BizException("类型有误");
      }

      // 2. 判断文件是否支持
      String fileSuffix = FileKit.getFileSuffix(file.getOriginalFilename(), false);
      if (!ossFileConfig.isAllowFileSuffix(fileSuffix)) {
        throw new BizException("上传文件格式不支持！");
      }

      // 3. 判断文件大小是否超限
      if (!ossFileConfig.isMaxSizeLimit(file.getSize())) {
        throw new BizException("上传大小请限制在[" + ossFileConfig.getMaxSize() / 1024 / 1024 + "M]以内！");
      }
      ResellerOrderImportRequest resellerImportRequest = new ResellerOrderImportRequest();
      resellerImportRequest.setCurrentUserId(getCurrentUser().getSysUser().getSysUserId());
      resellerImportRequest.setProductType(productType);
      resellerImportRequest.setMultipartFile(file);
      resellerOrderImportService.batchImport(resellerImportRequest);
      return ApiRes.ok();
    } catch (BizException biz) {
      throw biz;
    } catch (Exception e) {
      logger.error("upload error, fileName = {}", file.getOriginalFilename(), e);
      throw new BizException(ApiCodeEnum.SYSTEM_ERROR);
    }
  }


  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_GROUP_LIST')")
  @GetMapping
  public ApiRes list() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    LambdaQueryWrapper<ResellerOrder> condition = ResellerOrder.gw();
    if (StringUtils.isNotEmpty(resellerOrder.getOrderNo())) {
      condition.eq(ResellerOrder::getOrderNo, resellerOrder.getOrderNo());
    }
    condition.eq(ResellerOrder::getResellerNo, getCurrentUser().getSysUser().getUserNo());
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      condition.like(ResellerOrder::getProductType, resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getChargeAccount())) {
      condition.like(ResellerOrder::getChargeAccount, resellerOrder.getChargeAccount());
    }
    if (resellerOrder.getAmount() != null) {
      condition.eq(ResellerOrder::getAmount,
          AmountUtil.convertDollar2Cent(resellerOrder.getAmount() + ""));
    }
    if (StringUtils.isNotBlank(resellerOrder.getQueryFlag())) {
      condition.like(ResellerOrder::getQueryFlag, resellerOrder.getQueryFlag());
    }

    if (StringUtils.isNotBlank(resellerOrder.getOrderStatus())) {
      condition.eq(ResellerOrder::getOrderStatus, resellerOrder.getOrderStatus());
    }

    if (StringUtils.isNotBlank(resellerOrder.getMatchOutTradeNo())) {
      condition.eq(ResellerOrder::getMatchOutTradeNo, resellerOrder.getMatchOutTradeNo());
    }
    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(ResellerOrder::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(ResellerOrder::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(ResellerOrder::getGmtCreate);
    IPage<ResellerOrder> pages = resellerOrderService.page(getIPage(true), condition);
    return ApiRes.page(pages);
  }

  @PreAuthorize("hasAuthority('ENT_RESELLER_ORDER_GROUP_VIEW')")
  @RequestMapping(value = "/{resellerOrderId}", method = RequestMethod.GET)
  public ApiRes detail(@PathVariable("resellerOrderId") Long resellerOrderId) {
    ResellerOrder resellerOrder = resellerOrderService.getById(resellerOrderId);
    if (resellerOrder == null) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
    }
    return ApiRes.ok(resellerOrder);
  }

}
