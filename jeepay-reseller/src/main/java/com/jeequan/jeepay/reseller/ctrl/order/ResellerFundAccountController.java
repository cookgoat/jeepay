package com.jeequan.jeepay.reseller.ctrl.order;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jeequan.jeepay.core.entity.*;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.params.reseller.ResellerFundAccountParams;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.reseller.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.ResellerFundAccountService;
import com.jeequan.jeepay.service.impl.ResellerFundAccountSnapshotService;
import com.jeequan.jeepay.service.impl.ResellerService;
import com.jeequan.jeepay.service.impl.SysUserService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resellerFundAccounts")
@Slf4j
public class ResellerFundAccountController extends CommonCtrl {
    @Autowired
    private ResellerFundAccountService resellerFundAccountService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ResellerFundAccountSnapshotService resellerFundAccountSnapshotService;
    @Autowired
    private ResellerService resellerService;

    @RequestMapping(method = RequestMethod.GET)
    public ApiRes list(ResellerFundAccountParams params) {
        SysUser user = JeeUserDetails.getCurrentUserDetails().getSysUser();
        params.setResellerNo(user.getUserNo());
        return ApiRes.ok(resellerFundAccountService.page(getIPage(true),params));
    }

    @RequestMapping(value = "/statByToday", method = RequestMethod.GET)
    public ApiRes statByToday() {
        SysUser user = JeeUserDetails.getCurrentUserDetails().getSysUser();
        LambdaQueryWrapper<ResellerFundAccountSnapshot> condition = ResellerFundAccountSnapshot.gw();
        condition.eq(ResellerFundAccountSnapshot::getResellerNo,user.getUserNo());

        condition.ge(ResellerFundAccountSnapshot::getGmtCreate,getStartOfDay(new Date()));
        condition.le(ResellerFundAccountSnapshot::getGmtCreate,getEndOfDay(new Date()));

        return ApiRes.ok(resellerFundAccountSnapshotService.getOne(condition));
    }

    public static String getStartOfDay(Date time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
    }

    public static String getEndOfDay(Date time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        return new SimpleDateFormat(("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
    }

    @RequestMapping(value = "/statByAll", method = RequestMethod.GET)
    public ApiRes statByAll() {
        SysUser user = JeeUserDetails.getCurrentUserDetails().getSysUser();
        LambdaQueryWrapper<ResellerFundAccount> condition = ResellerFundAccount.gw();
        condition.eq(ResellerFundAccount::getResellerNo,user.getUserNo());
        return ApiRes.ok(resellerFundAccountService.getOne(condition));
    }

    /** 商户基本信息、用户基本信息 **/
    @RequestMapping(value="userDetail", method = RequestMethod.GET)
    public ApiRes userDetail() {
        SysUser sysUser = sysUserService.getById(getCurrentUser().getSysUser().getSysUserId());
        Reseller reseller = resellerService.getById(getCurrentUser().getSysUser().getUserNo());
        JSONObject json = (JSONObject) JSON.toJSON(reseller);
        json.put("loginUsername", sysUser.getLoginUsername());
        json.put("realname", sysUser.getRealname());
        return ApiRes.ok(json);
    }

}
