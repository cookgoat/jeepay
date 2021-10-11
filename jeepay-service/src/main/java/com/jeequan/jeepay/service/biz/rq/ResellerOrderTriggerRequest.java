package com.jeequan.jeepay.service.biz.rq;

import lombok.Data;
import java.util.List;

/**
 * @author axl rose
 * @date 2021/10/11
 */
@Data
public class ResellerOrderTriggerRequest {
  private List<Long>  resellerOrderIds;
  private String resellerOrderStatus;
}
