package com.jeequan.jeepay.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeequan.jeepay.core.constants.ResellerOrderStatusEnum;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.service.impl.ResellerOrderService;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author axl rose
 * @date 2021/10/2
 */
@Slf4j
@Component
public class ResellerOrderStatusRoolback {

  @Autowired
  private ResellerOrderService resellerOrderService;

  private static final int QUERY_PAGE_SIZE = 100; //每次查询数量


  @Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
  public void start() {

    //当前时间 减去1分钟。
    Date offsetDate = DateUtil.offsetMinute(new Date(), -1);

    //查询条件： 支付中的订单 & （ 订单创建时间 + 1分钟 >= 当前时间 ）
    LambdaQueryWrapper<ResellerOrder> lambdaQueryWrapper = ResellerOrder.gw().eq(ResellerOrder::getOrderStatus,
        ResellerOrderStatusEnum.MATCHING).le(ResellerOrder::getGmtUpdate, offsetDate);

    int currentPageIndex = 1; //当前页码
    while(true){

      try {
        IPage<ResellerOrder> payOrderIPage = resellerOrderService.page(new Page(currentPageIndex, QUERY_PAGE_SIZE), lambdaQueryWrapper);

        if(payOrderIPage == null || payOrderIPage.getRecords().isEmpty()){ //本次查询无结果, 不再继续查询;
          break;
        }

        for(ResellerOrder payOrder: payOrderIPage.getRecords()){
          resellerOrderService.update(new LambdaUpdateWrapper<ResellerOrder>().set(ResellerOrder::getOrderStatus, ResellerOrderStatusEnum.WAITING_MATCH)
              .eq(ResellerOrder::getId,payOrder.getId()));
        }

        //已经到达页码最大量，无需再次查询
        if(payOrderIPage.getPages() <= currentPageIndex){
          break;
        }
        currentPageIndex++;


      } catch (Exception e) { //出现异常，直接退出，避免死循环。
        log.error("error", e);
        break;
      }

    }
  }


}
