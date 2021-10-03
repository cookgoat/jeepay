package com.jeequan.jeepay.pay.pretender.match;

import com.jeequan.jeepay.pay.server.MatchPayDtaRs;

/**
 * pay order match a pretender order
 * @author axl rose
 */
public interface OrderAssociateMatcher {

  /**
   * Associate a pay order of pretender order
   * @param orderId
   * @return MatchPayDtaRs match info
   */
  MatchPayDtaRs matchOrder(String orderId);

}
