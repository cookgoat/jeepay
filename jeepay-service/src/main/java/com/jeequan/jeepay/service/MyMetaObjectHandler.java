package com.jeequan.jeepay.service;

import java.util.Date;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

@Component
public class MyMetaObjectHandler  implements MetaObjectHandler {

  /**
   * 插入时的填充策略
   * @param metaObject
   */
  @Override
  public void insertFill(MetaObject metaObject) {
    this.setFieldValByName("gmtCreate", new Date(), metaObject);
    this.setFieldValByName("gmtUpdate", new Date(), metaObject);
  }

  /**
   * 更新时的填充策略
   * @param metaObject
   */
  @Override
  public void updateFill(MetaObject metaObject) {
    this.setFieldValByName("gmtUpdate", new Date(), metaObject);
  }
}
