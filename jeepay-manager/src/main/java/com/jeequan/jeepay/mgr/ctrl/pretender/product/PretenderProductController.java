package com.jeequan.jeepay.mgr.ctrl.pretender.product;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.model.ApiRes;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.entity.PretenderProduct;
import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.jeequan.jeepay.service.impl.PretenderProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@RestController
@RequestMapping("/api/pretenderProduct")
@Slf4j
public class PretenderProductController extends CommonCtrl {

  @Autowired
  private PretenderProductService pretenderProductService;

  @PreAuthorize("hasAnyAuthority('ENT_PRETENDER_PRODUCT_GROUP_LIST')")
  @GetMapping
  public ApiRes list() {
    PretenderProduct pretenderProduct = getObject(PretenderProduct.class);
    LambdaQueryWrapper<PretenderProduct> condition = PretenderProduct.gw();
    if (StringUtils.isNotBlank(pretenderProduct.getProductType())) {
      condition.eq(PretenderProduct::getProductType, pretenderProduct.getProductType());
    }
    if (StringUtils.isNotBlank(pretenderProduct.getProductName())) {
      condition.like(PretenderProduct::getProductName, pretenderProduct.getProductName());
    }
    if (StringUtils.isNotBlank(pretenderProduct.getStatus())) {
      condition.eq(PretenderProduct::getStatus, pretenderProduct.getStatus());
    }

    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(PretenderProduct::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(PretenderProduct::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(PretenderProduct::getGmtCreate);
    IPage<PretenderProduct> pages = pretenderProductService.page(getIPage(true), condition);
    return ApiRes.page(pages);
  }

  @PreAuthorize("hasAuthority('ENT_PRETENDER_PRODUCT_GROUP_ADD')")
  @MethodLog(remark = "新增产品")
  @RequestMapping(value = "", method = RequestMethod.POST)
  public ApiRes add() {
    PretenderProduct pretenderProduct = getObject(PretenderProduct.class);
    // 当前登录用户信息
    SysUser currentSysUser = getCurrentUser().getSysUser();
    pretenderProduct.setCreateUid(currentSysUser.getSysUserId());
    pretenderProduct.setStatus(SwitchStatusEnum.ENABLE.getCode());
    pretenderProductService.save(pretenderProduct);
    return ApiRes.ok();
  }

  @PreAuthorize("hasAuthority('ENT_PRETENDER_PRODUCT_GROUP_EDIT')")
  @PutMapping("/{productId}")
  @MethodLog(remark = "更新支付产品")
  public ApiRes update(@PathVariable("productId") Long productId) {
    PretenderProduct pretenderProduct = getObject(PretenderProduct.class);
    pretenderProduct.setId(productId);
    boolean result = pretenderProductService.updateById(pretenderProduct);
    if (!result) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }
    return ApiRes.ok();
  }

  @PreAuthorize("hasAuthority('ENT_PRETENDER_PRODUCT_GROUP_VIEW')")
  @RequestMapping(value = "/{productId}", method = RequestMethod.GET)
  public ApiRes detail(@PathVariable("productId") Long productId) {

    PretenderProduct pretenderProduct = pretenderProductService.getById(productId);
    if (pretenderProduct == null) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
    }
    return ApiRes.ok(pretenderProduct);
  }


}
