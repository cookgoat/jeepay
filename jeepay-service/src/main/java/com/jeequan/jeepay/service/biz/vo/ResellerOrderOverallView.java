package com.jeequan.jeepay.service.biz.vo;

import lombok.Data;

/**
 * @author axl rose
 * @date 2021/10/5
 */
@Data
public class ResellerOrderOverallView {
  private Long faceAmount=0L;
  private Long allCount=0L;
  private Long allAmount=0L;
  private Long waitCount=0L;
  private Long waitAllAmount=0L;
  private Long payingCount=0L;
  private Long payAllAmount=0L;
  private Long finishCount=0L;
  private Long finishAllAmount=0L;
  private Long sleepCount=0L;
  private Long sleepAllAmount=0L;
}
