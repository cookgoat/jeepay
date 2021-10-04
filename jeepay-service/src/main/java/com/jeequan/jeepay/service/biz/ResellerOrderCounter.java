package com.jeequan.jeepay.service.biz;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeequan.jeepay.core.entity.ResellerOrder;
import com.jeequan.jeepay.service.biz.vo.ResellerOrderCountVo;
import java.util.List;

public interface ResellerOrderCounter {

  IPage<ResellerOrderCountVo> getResellerCounterPage(ResellerOrder resellerOrder,String startDay,String endDay,IPage iPage);
   List<ResellerOrderCountVo> getResellerCounterList(ResellerOrder resellerOrder,
      String startDay, String endDay);
}
