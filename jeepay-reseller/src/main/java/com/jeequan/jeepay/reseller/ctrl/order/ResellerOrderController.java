package com.jeequan.jeepay.reseller.ctrl.order;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.components.oss.model.OssFileConfig;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.ProductTypeEnum;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.core.utils.AmountUtil;
import com.jeequan.jeepay.core.utils.ExcelUtil;
import com.jeequan.jeepay.core.utils.FileKit;
import com.jeequan.jeepay.reseller.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.biz.ResellerOrderCounter;
import com.jeequan.jeepay.service.biz.ResellerOrderImportService;
import com.jeequan.jeepay.service.biz.ResellerOrderStatusTrigger;
import com.jeequan.jeepay.service.biz.fileentity.ResellerOrderExportEntity;
import com.jeequan.jeepay.service.biz.rq.ResellerOrderImportRequest;
import com.jeequan.jeepay.service.biz.rq.ResellerOrderTriggerRequest;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderCountVo;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderFundOverallView;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderOverallView;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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

  @Autowired
  private ResellerOrderCounter resellerOrderCounter;

  @Autowired
  private ResellerOrderStatusTrigger resellerOrderStatusTrigger;

  private static SimpleDateFormat sdf = new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss"));

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
    condition.orderByDesc(ResellerOrder::getId);
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

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_GROUP_EXPORT')")
  @GetMapping(value = "export")
  public void export(HttpServletResponse httpServletResponse) {

    ResellerOrder resellerOrder = getObject(ResellerOrder.class);

    LambdaQueryWrapper<ResellerOrder> condition = ResellerOrder.gw();
    if (StringUtils.isNotEmpty(resellerOrder.getOrderNo())) {
      condition.eq(ResellerOrder::getOrderNo, resellerOrder.getOrderNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getResellerNo())) {
      condition.eq(ResellerOrder::getResellerNo, resellerOrder.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerOrder.getProductType())) {
      condition.like(ResellerOrder::getProductType, resellerOrder.getProductType());
    }
    if (StringUtils.isNotBlank(resellerOrder.getChargeAccount())) {
      condition.like(ResellerOrder::getChargeAccount, resellerOrder.getChargeAccount());
    }
    if (resellerOrder.getAmount() != null) {
      condition.eq(ResellerOrder::getAmount,
          resellerOrder.getAmount());
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
    condition.orderByDesc(ResellerOrder::getId);
    List<ResellerOrder> resellerOrderList = resellerOrderService.list(condition);
    if (resellerOrderList.size() > 0) {
      ExcelUtil.exportExcel(convertTo(resellerOrderList), "核销订单", "核销订单",
          ResellerOrderExportEntity.class, getToday(new Date()) + "-核销订单", httpServletResponse);
    }
  }


  List<ResellerOrderExportEntity> convertTo(List<ResellerOrder> resellerOrderList) {
    List<ResellerOrderExportEntity> resellerOrderExportEntityList = new ArrayList<>();
    for (ResellerOrder resellerOrder : resellerOrderList) {
      ResellerOrderExportEntity resellerOrderExportEntity = new ResellerOrderExportEntity();
      resellerOrderExportEntity.setResellerNo(resellerOrder.getResellerNo());
      resellerOrderExportEntity.setOrderNo(resellerOrder.getOrderNo());
      resellerOrderExportEntity.setOrderStatus(
          ResellerOrderStatusEnum.getType(resellerOrder.getOrderStatus()).getMsg());
      resellerOrderExportEntity.setAmount(AmountUtil.convertCent2Dollar(resellerOrder.getAmount()));
      resellerOrderExportEntity.setChargeAccount(resellerOrder.getChargeAccount());
      resellerOrderExportEntity.setProductType(
          ProductTypeEnum.getType(resellerOrder.getProductType()).getMsg());
      resellerOrderExportEntity.setGmtCreate(sdf.format(resellerOrder.getGmtCreate()));
      resellerOrderExportEntity.setGmtUpdate(sdf.format(resellerOrder.getGmtUpdate()));
      resellerOrderExportEntityList.add(resellerOrderExportEntity);
    }
    return resellerOrderExportEntityList;
  }

  public static String getToday(Date time) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(time);
    return new SimpleDateFormat(("yyyy-MM-dd")).format(calendar.getTime());
  }


  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_COUNT_LIST')")
  @GetMapping(value = "resellerCounterPage")
  public ApiRes getResellerCounterPage() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JSONObject paramJSON = getReqParamJSON();
    JeeUserDetails sysUser = getCurrentUser();
    resellerOrder.setResellerNo(sysUser.getSysUser().getUserNo());
    IPage<ResellerOrderCountVo> pages = resellerOrderCounter.getResellerCounterPage(resellerOrder,
        paramJSON.getString("createdStart"), paramJSON.getString("createdEnd")
        , getIPage());
    return ApiRes.page(pages);
  }

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_EXPORT_COUNT')")
  @GetMapping(value = "exportResellerCounter")
  public void exportResellerCounter(HttpServletResponse httpServletResponse) {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JSONObject paramJSON = getReqParamJSON();
    JeeUserDetails sysUser = getCurrentUser();
    resellerOrder.setResellerNo(sysUser.getSysUser().getUserNo());
    List<ResellerOrderCountVo> pages = resellerOrderCounter.getResellerCounterList(resellerOrder,
        paramJSON.getString("createdStart"), paramJSON.getString("createdEnd"));
    if (pages.size() > 0) {
      ExcelUtil.exportExcel(pages, "核销订单报表统计", "核销订单报表统计",
          ResellerOrderCountVo.class, getToday(new Date()) + "-核销订单报表统计", httpServletResponse);
    }
  }

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_OVERALL_COUNT')")
  @GetMapping(value = "overallResellerCount")
  public ApiRes overallResellerCount() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JeeUserDetails sysUser = getCurrentUser();
    Date now = new Date();
    resellerOrder.setResellerNo(sysUser.getSysUser().getUserNo());
    List<ResellerOrderOverallView> resellerOrderOverallViewList = resellerOrderCounter
        .countOverallViewByAmount(resellerOrder, getStartOfDay(now), getEndOfDay(now));
    return ApiRes.ok(resellerOrderOverallViewList);
  }


  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_OVERALL_COUNT')")
  @GetMapping(value = "orderFundOverallView")
  public ApiRes orderFundOverallView() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JeeUserDetails sysUser = getCurrentUser();
    resellerOrder.setResellerNo(sysUser.getSysUser().getUserNo());
    Date now = new Date();
    List<ResellerOrderFundOverallView> resellerOrderOverallViewList = resellerOrderCounter
        .countReSellerOrderFundByReseller(getStartOfDay(now), getEndOfDay(now),
            resellerOrder);
    ResellerOrderFundOverallView resellerOrderFundOverallView;
    if (resellerOrderOverallViewList.size() > 0) {
      resellerOrderFundOverallView = resellerOrderOverallViewList.get(0);
    } else {
      resellerOrderFundOverallView = new ResellerOrderFundOverallView();
    }
    return ApiRes.ok(resellerOrderFundOverallView);
  }


  public static String getStartOfDay(Date time) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(time);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
  }

  public static String getEndOfDay(Date time) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(time);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
  }

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_ORDER_OVERALL_COUNT')")
  @GetMapping(value = "orderFundOverallViewByChildReseller")
  public ApiRes orderFundOverallViewByChildReseller() {
    ResellerOrder resellerOrder = getObject(ResellerOrder.class);
    JeeUserDetails sysUser = getCurrentUser();
    resellerOrder.setResellerNo(sysUser.getSysUser().getUserNo());
    Date now = new Date();
    List<ResellerOrderFundOverallView> resellerOrderOverallViewList = resellerOrderCounter
        .countReSellerOrderFundByQueryFlag(getStartOfDay(now), getEndOfDay(now), resellerOrder);
    return ApiRes.ok(resellerOrderOverallViewList);
  }

  /**
   * 批量警用、启用
   */
  @PreAuthorize("hasAuthority('ENT_RESELLER_ORDER_GROUP_IMPORT')")
  @PostMapping("trigger")
  public ApiRes batchTrigger() {
    ResellerOrderTriggerRequest resellerOrderTriggerRequest = getObject(
        ResellerOrderTriggerRequest.class);
    if (StringUtils.equalsIgnoreCase(resellerOrderTriggerRequest.getResellerOrderStatus(),
        ResellerOrderStatusEnum.NULLIFY.getCode())) {
      resellerOrderStatusTrigger.batchDisable(resellerOrderTriggerRequest);
    }
    if (StringUtils.equalsIgnoreCase(resellerOrderTriggerRequest.getResellerOrderStatus(),
        ResellerOrderStatusEnum.WAIT_CHARGE.getCode())) {
      resellerOrderStatusTrigger.batchEnable(resellerOrderTriggerRequest);
    }
    return ApiRes.ok();
  }

}
