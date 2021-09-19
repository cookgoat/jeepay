package com.jeequan.jeepay.core.utils;

import java.util.Map;
import java.net.Proxy;
import org.jsoup.Jsoup;
import java.io.IOException;
import org.jsoup.Connection;
import java.net.Authenticator;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import java.net.PasswordAuthentication;
import org.apache.commons.lang3.StringUtils;
import static com.jeequan.jeepay.core.constants.CS.HEADERS.ACCEPT_XHTML_XML;
import static com.jeequan.jeepay.core.constants.CS.HEADERS.FORM_URL_ENCODE;

/**
 * @author axl rose
 * @date 2021/9/8
 */
public class JsoupUtils {

    public  String httpGet(String url, String cookie, Proxy proxy,String userName,String password) throws IOException {
        //是否需要身份认证
        if(proxy!=null&&StringUtils.isNotBlank(userName)){
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password.toCharArray());
                }
            });
        }
        //获取请求连接
        Connection con = Jsoup.connect(url);
        if(proxy!=null){
            con.proxy(proxy);
        }
        //请求头设置，特别是cookie设置
        con.header("Accept", ACCEPT_XHTML_XML);
        con.header("User-Agent", UserAgentUtil.randomUserAgent());
        con.header("Cookie", cookie);
        con.method(Method.GET);
        //解析请求结果
        Response response = con.execute();
        Document doc = response.parse();
        //获取标题
        System.out.println(doc.title());
        return doc.body().wholeText();
    }

    public  String httpGet(String url, Map<String,String> headersMap,Proxy proxy,String userName,String password) throws IOException {
        //是否需要身份认证
        if(proxy!=null&&StringUtils.isNotBlank(userName)){
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password.toCharArray());
                }
            });
        }
        //获取请求连接
        Connection con = Jsoup.connect(url);
        if(proxy!=null){
            con.proxy(proxy);
        }
        //请求头设置，特别是cookie设置
        con.headers(headersMap);
        //解析请求结果
        Document doc = con.get();
        //获取标题
        System.out.println(doc.title());
        return doc.toString();
    }

    public  String httpPost(String url, Map<String, String> map, String cookie) throws IOException {
        //获取请求连接
        Connection con = Jsoup.connect(url);
        //遍历生成参数
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                //添加参数
                con.data(entry.getKey(), entry.getValue());
            }
        }
        //插入cookie（头文件形式）
        con.header("Cookie", cookie);
        Document doc = con.post();
        System.out.println(doc);
        return doc.toString();
    }

    public  String httpReturnRespHeader(String url, String cook, String header, boolean isPost) throws IOException {
        //获取请求连接
        Connection con = Jsoup.connect(url);
        //请求头设置，特别是cookie设置
        con.header("Accept", ACCEPT_XHTML_XML);
        con.header("User-Agent", UserAgentUtil.randomUserAgent());
        con.header("Content-Type", FORM_URL_ENCODE);
        con.header("Cookie", cook);
        //发送请求
        Connection.Response resp;
        if (isPost) {
            resp = con.method(Connection.Method.POST).execute();

        } else {
            resp = con.method(Connection.Method.GET).execute();
        }
        //获取cookie名称为__bsi的值
        String cookieValue = resp.cookie(header);
        System.out.println(cookieValue);
        //获取返回cookie所值
        Map<String, String> cookies = resp.cookies();
        System.out.println("所有cookie值：  " + cookies);
        //获取返回头文件值
        String headerValue = resp.header(header);
        System.out.println("头文件" + header + "的值：" + headerValue);
        //获取所有头文件值
        Map<String, String> headersOne = resp.headers();
        System.out.println("所有头文件值：" + headersOne);
        return headerValue;
    }

}
