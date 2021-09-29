package com.jeequan.jeepay.mgr.ctrl.reseller;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.aop.MethodLog;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.entity.SysUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.entity.PretenderProduct;
import com.jeequan.jeepay.mgr.ctrl.vo.ResellerProductVo;
import org.springframework.web.bind.annotation.GetMapping;
import com.jeequan.jeepay.core.constants.SwitchStatusEnum;
import org.springframework.web.bind.annotation.RequestMethod;
import com.jeequan.jeepay.service.biz.ResellerSettleUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jeequan.jeepay.service.impl.PretenderProductService;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;
import org.springframework.security.access.prepost.PreAuthorize;
import com.jeequan.jeepay.service.impl.ResellerPretenderProductService;
import com.jeequan.jeepay.service.biz.PretenderResellerProductConfigurer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@RestController
@RequestMapping("/api/resellerProducts")
@Slf4j
public class ResellerProductController extends CommonCtrl {

  @Autowired
  private PretenderResellerProductConfigurer pretenderResellerProductConfigurer;

  @Autowired
  private ResellerPretenderProductService resellerPretenderProductService;

  @Autowired
  private PretenderProductService pretenderProductService;

  @Autowired
  private ResellerSettleUpService resellerSettleUpService;

  @PreAuthorize("hasAuthority('ENT_RESELLER_PRODUCT_GROUP_CONFIG')")
  @MethodLog(remark = "设置核销商核销产品")
  @RequestMapping(method = RequestMethod.POST)
  public ApiRes configResellerProduct() {
    ResellerPretenderProduct resellerPretenderProduct = getObject(ResellerPretenderProduct.class);
    // 当前登录用户信息
    SysUser currentSysUser = getCurrentUser().getSysUser();
    pretenderResellerProductConfigurer.config(resellerPretenderProduct, currentSysUser);
    return ApiRes.ok();
  }

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_PRODUCT_GROUP_LIST')")
  @GetMapping
  public ApiRes list() {
    ResellerPretenderProduct resellerPretenderProduct = getObject(ResellerPretenderProduct.class);
    LambdaQueryWrapper<ResellerPretenderProduct> condition = ResellerPretenderProduct.gw();
    if (StringUtils.isNotBlank(resellerPretenderProduct.getProductType())) {
      condition
          .eq(ResellerPretenderProduct::getProductType, resellerPretenderProduct.getProductType());
    }
    if (StringUtils.isNotBlank(resellerPretenderProduct.getResellerNo())) {
      condition
          .like(ResellerPretenderProduct::getResellerNo, resellerPretenderProduct.getResellerNo());
    }
    if (StringUtils.isNotBlank(resellerPretenderProduct.getStatus())) {
      condition.eq(ResellerPretenderProduct::getStatus, resellerPretenderProduct.getStatus());
    }

    JSONObject paramJSON = getReqParamJSON();
    if (paramJSON != null) {
      if (StringUtils.isNotEmpty(paramJSON.getString("createdStart"))) {
        condition.ge(ResellerPretenderProduct::getGmtCreate, paramJSON.getString("createdStart"));
      }
      if (StringUtils.isNotEmpty(paramJSON.getString("createdEnd"))) {
        condition.le(ResellerPretenderProduct::getGmtCreate, paramJSON.getString("createdEnd"));
      }
    }
    condition.orderByDesc(ResellerPretenderProduct::getGmtCreate);
    IPage<ResellerPretenderProduct> pages = resellerPretenderProductService
        .page(getIPage(true), condition);
    return ApiRes.page(pages);
  }

  @PreAuthorize("hasAnyAuthority('ENT_RESELLER_PRODUCT_GROUP_QUERY')")
  @GetMapping(value = "/list")
  public ApiRes queryResellerList() {
    ResellerPretenderProduct param = getObject(ResellerPretenderProduct.class);
    if (StringUtils.isBlank(param.getResellerNo())) {
      throw new BizException("需要核销商号");
    }
    Map<String, ResellerPretenderProduct> resellerPretenderProductMap = queryCurrentResellerProductMap(
        param.getResellerNo());
    List<PretenderProduct> pretenderProducts = queryEnableProduct();
    List<ResellerProductVo> resellerProductVoList = new ArrayList<>();
    for (PretenderProduct pretenderProduct : pretenderProducts) {
      ResellerProductVo resellerProductVo = new ResellerProductVo();
      resellerProductVo.setProductName(pretenderProduct.getProductName());
      resellerProductVo.setProductType(pretenderProduct.getProductType());
      resellerProductVo.setResellerNo(param.getResellerNo());
      ResellerPretenderProduct resellerEnablePretenderProduct = resellerPretenderProductMap
          .get(pretenderProduct.getProductType());
      if (resellerEnablePretenderProduct != null) {
        resellerProductVo.setStatus(SwitchStatusEnum.ENABLE.getCode());
        resellerProductVo.setCreditAmount(resellerEnablePretenderProduct.getCreditAmount());
        resellerProductVo.setFeeRate(resellerEnablePretenderProduct.getFeeRate());
      }
      resellerProductVoList.add(resellerProductVo);
    }
    return ApiRes.ok(resellerProductVoList);
  }

  private List<PretenderProduct> queryEnableProduct() {
    return pretenderProductService.list(PretenderProduct.gw().eq(
        PretenderProduct::getStatus, SwitchStatusEnum.ENABLE
    ));
  }

  private Map<String, ResellerPretenderProduct> queryCurrentResellerProductMap(String resellerNo) {
    Map<String, ResellerPretenderProduct> resellerPretenderProductMap = new HashMap<>(2);
    LambdaQueryWrapper<ResellerPretenderProduct> condition = ResellerPretenderProduct.gw();
    condition.eq(ResellerPretenderProduct::getResellerNo, resellerNo);
    condition.orderByDesc(ResellerPretenderProduct::getGmtUpdate);
    List<ResellerPretenderProduct> currentResellerProducts = resellerPretenderProductService
        .list(condition);
    Map<String, List<ResellerPretenderProduct>> productTypeResellerProductMap = currentResellerProducts
        .stream().collect(Collectors.groupingBy(ResellerPretenderProduct::getProductType));
    for (String productType : productTypeResellerProductMap.keySet()) {
      Optional<ResellerPretenderProduct> resellerPretenderOpt = productTypeResellerProductMap
          .get(productType).stream().filter(resellerPretenderProduct -> StringUtils
              .equalsIgnoreCase(resellerPretenderProduct.getStatus(),
                  SwitchStatusEnum.ENABLE.getCode())).findAny();
          if(resellerPretenderOpt.isPresent()){
            resellerPretenderProductMap.put(productType, resellerPretenderOpt.get());
          }
    }
    return resellerPretenderProductMap;
  }

  @PreAuthorize("hasAuthority('ENT_RESELLER_PRODUCT_GROUP_SETTLEUP')")
  @MethodLog(remark = "清账")
  @RequestMapping(value = "settleUp",method = RequestMethod.POST)
  public ApiRes settleUp() {
    ResellerPretenderProduct resellerPretenderProduct = getObject(ResellerPretenderProduct.class);
    // 当前登录用户信息
    SysUser currentSysUser = getCurrentUser().getSysUser();
    resellerSettleUpService.settleUp(resellerPretenderProduct.getResellerNo(),resellerPretenderProduct.getProductType());
    return ApiRes.ok();
  }

}
