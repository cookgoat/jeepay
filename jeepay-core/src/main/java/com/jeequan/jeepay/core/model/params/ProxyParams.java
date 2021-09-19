package com.jeequan.jeepay.core.model.params;

import java.net.Proxy;
import lombok.Data;

@Data
public class ProxyParams {
  private Proxy proxy;
  private String userName;
  private String password;
}
