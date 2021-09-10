package com.jeequan.jeepay.pay.channel.propertycredit.kits;

import com.jeequan.jeepay.core.utils.UserAgentUtil;
import jodd.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class HeaderHelper {
    static Map<String, String> buildCommonHeaders(String cookie) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Host", CS.HOST);
        headers.put("User-Agent", UserAgentUtil.randomUserAgent());
        headers.put("Accept", com.jeequan.jeepay.core.constants.CS.HEADERS.ACCEPT_ALL);
        headers.put("Accept-Language", com.jeequan.jeepay.core.constants.CS.HEADERS.ACCEPT_LANGUAGE);
        headers.put("Accept-Encoding", com.jeequan.jeepay.core.constants.CS.HEADERS.ACCEPT_ENCODING);
        headers.put("Content-Type", com.jeequan.jeepay.core.constants.CS.HEADERS.FORM_URL_ENCODE);
        headers.put("X-Requested-With", com.jeequan.jeepay.core.constants.CS.HEADERS.X_Requested_With);
        headers.put("Connection", "close");
        if (StringUtil.isNotBlank(cookie)) {
            headers.put("Cookie", cookie);
        }
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        return headers;
    }
}