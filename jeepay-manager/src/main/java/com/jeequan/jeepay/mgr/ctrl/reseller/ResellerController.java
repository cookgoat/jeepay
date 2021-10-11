package com.jeequan.jeepay.mgr.ctrl.reseller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.service.impl.ResellerService;
import lombok.extern.slf4j.Slf4j;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.entity.Reseller;
import com.jeequan.jeepay.service.biz.ResellerAddService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/resellers")
@Slf4j
public class ResellerController extends CommonCtrl {

  @Autowired
  private ResellerAddService resellerAddService;

  @Autowired
  private ResellerService resellerService;

  @PreAuthorize("hasAuthority('ENT_RESELLER_ACCOUNT_GROUP_ADD')")
  @MethodLog(remark = "新增核销商")
  @RequestMapping(value = "", method = RequestMethod.POST)
  public ApiRes add() {
    Reseller reseller = getObject(Reseller.class);
    // 获取传入的商户登录名
    String loginUserName = getValStringRequired("loginUserName");
    // 当前登录用户信息
    SysUser currentSysUser = getCurrentUser().getSysUser();
    reseller.setCreateName(currentSysUser.getRealname());
    reseller.setCreateUid(currentSysUser.getSysUserId());
    resellerAddService.add(reseller, loginUserName);
    return ApiRes.ok();
  }


  @PreAuthorize("hasAuthority('ENT_RESELLER_ACCOUNT_GROUP_LIST')")
  @MethodLog(remark = "分页查询核销商")
  @GetMapping
  public ApiRes list() {
    Reseller reseller = getObject(Reseller.class);

    LambdaQueryWrapper<Reseller> condition = Reseller.gw();

    if (StringUtils.isNotEmpty(reseller.getResellerName())) {
      condition.like(Reseller::getResellerName, reseller.getResellerName());
    }
    if (StringUtils.isNotEmpty(reseller.getResellerNo())) {
      condition.eq(Reseller::getResellerNo, reseller.getResellerNo());
    }
    if (StringUtils.isNotEmpty(reseller.getStatus())) {
      condition.eq(Reseller::getStatus, reseller.getStatus());
    }
    if (reseller.getInitUserId() != null) {
      condition.eq(Reseller::getInitUserId, reseller.getInitUserId());
    }
    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(Reseller::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(Reseller::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(Reseller::getGmtCreate);
    IPage<Reseller> pages = resellerService.page(getIPage(true), condition);
    return ApiRes.page(pages);
  }


  @PreAuthorize("hasAuthority('ENT_RESELLER_ACCOUNT_GROUP_VIEW')")
  @RequestMapping(value = "/{resellerId}", method = RequestMethod.GET)
  @MethodLog(remark = "查询核销商详情")
  public ApiRes detail(@PathVariable("resellerId") Long resellerId) {
    Reseller reseller = resellerService.getById(resellerId);
    if (reseller == null) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELETE);
    }
    return ApiRes.ok(reseller);
  }

  @PreAuthorize("hasAuthority('ENT_RESELLER_ACCOUNT_GROUP_EDIT')")
  @PutMapping("/{resellerId}")
  @MethodLog(remark = "更新核销商信息")
  public ApiRes update(@PathVariable("resellerId") Long resellerId) {
    Reseller reseller = getObject(Reseller.class);
    reseller.setId(resellerId);
    boolean result = resellerService.updateById(reseller);
    if (!result) {
      return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
    }
    return ApiRes.ok();
  }





  //todo add del opt
}
