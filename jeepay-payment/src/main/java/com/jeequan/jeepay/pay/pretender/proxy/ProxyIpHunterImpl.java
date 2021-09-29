package com.jeequan.jeepay.pay.pretender.proxy;

import java.net.Proxy;
import com.ejlchina.okhttps.HTTP;
import java.net.InetSocketAddress;
import com.ejlchina.okhttps.OkHttps;
import com.ejlchina.okhttps.HttpResult;
import org.apache.commons.lang3.StringUtils;
import com.ejlchina.okhttps.HttpResult.State;
import org.springframework.stereotype.Service;
import com.ejlchina.okhttps.FastjsonMsgConvertor;
import com.jeequan.jeepay.pay.pretender.proxy.ProxyIpHunter;
import com.jeequan.jeepay.core.model.params.ProxyParams;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProxyIpHunterImpl implements ProxyIpHunter {

  private static final String separator = ":";
  private static final int RIGHT_LENGTH = 2;

  @Autowired
  private SysConfigService sysConfigService;


  @Override
  public ProxyParams huntProxy() {
    String proxyIpUrl = sysConfigService.getDBApplicationConfig().getProxyIpUrl();
    String ipPortStr = get(proxyIpUrl);
    if (StringUtils.isBlank(ipPortStr)) {
      return null;
    }
    String[] proxyArray = ipPortStr.split(separator);
    if (proxyArray.length != RIGHT_LENGTH) {
      return null;
    }
    Proxy proxy= new Proxy(Proxy.Type.HTTP,
        new InetSocketAddress(proxyArray[0], Integer.valueOf(proxyArray[1])));
    ProxyParams proxyParams = new ProxyParams();
    proxyParams.setProxy(proxy);
    proxyParams.setUserName("erma888999");
    proxyParams.setPassword("aakxyxs0");
    return proxyParams;
  }


  public static String get(String proxyIpApi) {
    HTTP http = HTTP.builder()
        .addMsgConvertor(new FastjsonMsgConvertor())
        .bodyType(OkHttps.FORM)
        .config(a -> a.followRedirects(false))
        .build();

    HttpResult httpResult = http.sync(proxyIpApi).get();
    if (httpResult.getState() == State.RESPONSED) {
      String ip = httpResult.getBody().toString();
      return ip;
    }
    return null;
  }

}
