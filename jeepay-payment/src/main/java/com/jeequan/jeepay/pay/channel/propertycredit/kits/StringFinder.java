package com.jeequan.jeepay.pay.channel.propertycredit.kits;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author axl rose
 * @date 2021/9/10
 */
public class StringFinder {

    public static final Pattern pattern = Pattern.compile("weixin?://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    public static String findWeChatDeeplinkFromString(String data){
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
