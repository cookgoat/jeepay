package com.jeequan.jeepay.service.biz;

import com.jeequan.jeepay.core.entity.SysUser;
import com.jeequan.jeepay.core.entity.ResellerPretenderProduct;

public interface PretenderResellerProductConfigurer {

  void config(ResellerPretenderProduct resellerPretenderProduct, SysUser sysUser);

}
