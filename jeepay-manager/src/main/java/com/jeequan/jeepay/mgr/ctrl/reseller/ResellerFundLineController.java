package com.jeequan.jeepay.mgr.ctrl.reseller;

import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.reseller.ResellerFundLineParams;
import com.jeequan.jeepay.mgr.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ResellerFundLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resellerFundLine")
@Slf4j
public class ResellerFundLineController extends CommonCtrl {
    @Autowired
    private ResellerFundLineService resellerFundLineService;

    @RequestMapping( method = RequestMethod.GET)
    public ApiRes list(ResellerFundLineParams params) {
        return ApiRes.ok(resellerFundLineService.page(getIPage(true),params));
    }
}
