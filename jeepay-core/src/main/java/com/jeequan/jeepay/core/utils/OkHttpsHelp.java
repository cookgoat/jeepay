package com.jeequan.jeepay.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ejlchina.okhttps.HttpResult;
import com.jeequan.jeepay.core.exception.BizException;

/**
 * @author axl rose
 * @date 2021/9/8
 */
public class OkHttpsHelp<T> {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpsHelp.class);


    public  T handleHttpResult(HttpResult httpResult,boolean isString,Class<T> clazz){
        // 判断执行状态
        switch (httpResult.getState()) {
            case RESPONSED: {
                // 请求已正常响应
                httpResult.getBody().cache();
                if(isString){
                   return (T) httpResult.getBody().toString();
                }
                T result = httpResult.getBody().toBean(clazz);
                return result;
            }
            case CANCELED: {
                httpResult.close();
                logger.error("[OkHttpsHelp.handleHttpResult] request canceled，httpResult={}",httpResult);
                throw new  BizException("request CANCELED");
            }
            case NETWORK_ERROR: {
                httpResult.close();
                logger.error("[OkHttpsHelp.handleHttpResult] network error，httpResult={}",httpResult);
                throw new  BizException("request NETWORK_ERROR");
            }
            case TIMEOUT: {
                httpResult.close();
                logger.error("[OkHttpsHelp.handleHttpResult] timeout，httpResult={}",httpResult);
                throw new  BizException("request TIMEOUT");

            }
            case EXCEPTION: {
                httpResult.close();
                logger.error("[OkHttpsHelp.handleHttpResult] exception，httpResult={}",httpResult);
                throw new  BizException("request EXCEPTION");
            }
            default: {
                httpResult.close();
                throw new  BizException("request failed");
            }
        }
    }

}
