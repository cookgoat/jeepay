package com.jeequan.jeepay.mgr.ctrl.reseller;

import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.reseller.ResellerFundAccountParams;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ResellerFundAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resellerFundAccount")
@Slf4j
public class ResellerFundAccountController extends CommonCtrl {
    @Autowired
    private ResellerFundAccountService resellerFundAccountService;

    @RequestMapping(method = RequestMethod.GET)
    public ApiRes list(ResellerFundAccountParams params) {
        return ApiRes.ok(resellerFundAccountService.page(getIPage(true),params));
    }
}
