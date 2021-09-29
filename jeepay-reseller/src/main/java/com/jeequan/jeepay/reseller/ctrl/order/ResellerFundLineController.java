package com.jeequan.jeepay.reseller.ctrl.order;

import lombok.extern.slf4j.Slf4j;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.entity.SysUser;
import org.springframework.web.bind.annotation.*;
import com.jeequan.jeepay.reseller.ctrl.CommonCtrl;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeequan.jeepay.service.impl.ResellerFundLineService;
import com.jeequan.jeepay.core.model.params.reseller.ResellerFundLineParams;

@RestController
@RequestMapping("/api/resellerFundLine")
@Slf4j
public class ResellerFundLineController extends CommonCtrl {
    @Autowired
    private ResellerFundLineService resellerFundLineService;

    @RequestMapping(method = RequestMethod.GET)
    public ApiRes list(ResellerFundLineParams params) {
        SysUser user = JeeUserDetails.getCurrentUserDetails().getSysUser();
        params.setResellerNo(user.getUserNo());
        return ApiRes.ok(resellerFundLineService.page(getIPage(true),params));
    }
}
