package com.jeequan.jeepay.service.biz.impl;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.SysUser;
import org.springframework.stereotype.Service;
import com.jeequan.jeepay.core.entity.Reseller;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.constants.CS.SYS_TYPE;
import com.jeequan.jeepay.service.impl.SysUserService;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.SnowflakeIdWorker;
import com.jeequan.jeepay.service.impl.ResellerService;
import com.jeequan.jeepay.service.biz.ResellerAddService;
import com.jeequan.jeepay.core.entity.ResellerFundAccount;
import com.jeequan.jeepay.core.constants.AccountStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.jeequan.jeepay.service.impl.ResellerFundAccountService;

@Service
public class ResellerAddServiceImpl implements ResellerAddService {

  private static final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(2, 2);

  @Autowired
  private ResellerService resellerService;

  @Autowired
  private SysUserService sysUserService;

  @Autowired
  private ResellerFundAccountService resellerFundAccountService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void add(Reseller reseller, String loginUserName) {
    setResellerCommonInfo(reseller);
    boolean saveResult = resellerService.save(reseller);
    if (!saveResult) {
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }

    // 插入用户信息
    SysUser sysUser = new SysUser();
    sysUser.setLoginUsername(loginUserName);
    sysUser.setRealname(reseller.getResellerName());
    sysUser.setUserNo(reseller.getResellerNo());
    sysUser.setBelongInfoId(reseller.getResellerNo());
    sysUser.setTelphone(reseller.getContactTel());
    sysUser.setSex(CS.SEX_MALE);
    sysUser.setIsAdmin(CS.YES);
    sysUser.setState(AccountStatusEnum.ENABLE.getCodeByte());
    sysUserService.addSysUser(sysUser, SYS_TYPE.RESELLER);

    // 存入核销商默认用户ID
    Reseller updateRecord = new Reseller();
    updateRecord.setResellerNo(reseller.getResellerNo());
    updateRecord.setInitUserId(sysUser.getSysUserId());
    updateRecord.setId(reseller.getId());
    saveResult = resellerService.updateById(updateRecord);
    if (!saveResult) {
      throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
    }
    saveResellerFundAccount(reseller);
  }

  private void setResellerCommonInfo(Reseller reseller) {
    reseller.setResellerNo("RU" + snowflakeIdWorker.nextId());
    reseller.setStatus(AccountStatusEnum.ENABLE.getCode());
    reseller.setLastUpdateUid(reseller.getCreateUid());
    reseller.setLastUpdateName(reseller.getCreateName());
  }

  private void saveResellerFundAccount(Reseller reseller){
    ResellerFundAccount resellerFundAccount = new ResellerFundAccount();
    resellerFundAccount.setResellerNo(reseller.getResellerNo());
    resellerFundAccount.setAllAmount(0L);
    resellerFundAccount.setRecoveriesAllAmount(0L);
    resellerFundAccount.setWaitAllAmount(0L);
    resellerFundAccount.setFinishedAllAmount(0L);
    resellerFundAccount.setShareAllAmount(0L);
    resellerFundAccountService.save(resellerFundAccount);
  }


}
