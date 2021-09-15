package com.jeequan.jeepay.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author axl rose
 * @date 2021/9/14
 */
public class RegexUtil {

    private static final Pattern p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$"); // 验证带区号的

    private static final Pattern p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$"); // 验证没有区号的

    private static final Pattern p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号

   private static final  Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");


    /**
     * 手机号验证
     *
     * @param str
     * @return 验证通过返回true
     */

    public static boolean isMobile(String str) {
        Matcher m ;
        boolean b ;
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 电话号码验证
     *
     * @param str
     * @return 验证通过返回true
     */

    public static boolean isPhone(String str) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        if (str.length() > 9) {
            m = p1.matcher(str);
            b = m.matches();
        } else {
            m = p2.matcher(str);
            b = m.matches();

        }
        return b;
    }


    public static boolean isInteger(String str) {
        return pattern.matcher(str).matches();
    }
}
