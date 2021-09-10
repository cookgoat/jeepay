package com.jeequan.jeepay.pay.channel.propertycredit.kits;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;


/**
 * @author axl rose
 * @date 2021/9/10
 */
public class AlipayHelper {

    /***
     *  get alipay pay params from alipay json result str
     * @param alipayResultJson
     * @return
     */
    public static Map<String,String> getAlipayUrlFromAction(String alipayResultJson){
        Map<String,String> map = new HashMap<>();
        String aHtml = JSONObject.parseObject(alipayResultJson).getString("data");
        Document document = Jsoup.parse(aHtml);
        Element element = document.select("form[name=punchout_form]").first();
        Elements actions =element.getAllElements();
        String action="";
        String param="";
        for(Element a:actions){
            if(a.attr("name").equals("punchout_form")){
                action=  a.attr("action");
                map.put("action",action);
            }
            if(a.attr("name").equals("biz_content")){
                param=  a.attr("value");
                map.put("biz_content",param);

            }
        }
        return map;
    }



}
