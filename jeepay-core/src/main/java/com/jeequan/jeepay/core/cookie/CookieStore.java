package com.jeequan.jeepay.core.cookie;

import java.util.List;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * @author axl rose
 * @date 2021/9/8
 */
public interface CookieStore {

    /**
     * 添加cookie
     * @param httpUrl
     * @param cookie
     */
    void add(HttpUrl httpUrl, Cookie cookie);

    /**
     * 添加指定http url cookie集合
     * @param httpUrl
     * @param cookies
     */
    void add(HttpUrl httpUrl, List<Cookie> cookies);

    /**
     * 根据HttpUrl从缓存中读取cookie集合
     * @param httpUrl
     * @return
     */
    List<Cookie> get(HttpUrl httpUrl);

    /**
     * 获取全部缓存cookie
     * @return
     */
    List<Cookie> getCookies();

    /**
     * 移除指定httpurl cookie集合
     * @param httpUrl
     * @param cookie
     * @return
     */
    boolean remove(HttpUrl httpUrl, Cookie cookie);

    /**
     * 移除所有cookie
     * @return
     */
    boolean removeAll();
}
