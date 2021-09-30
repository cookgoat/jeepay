package com.jeequan.jeepay.pay.channel;

import com.jeequan.jeepay.pay.server.MatchPayDtaRs;

public interface PretenderOrderMatcher {

  MatchPayDtaRs matchOrder(String orderId);

}
