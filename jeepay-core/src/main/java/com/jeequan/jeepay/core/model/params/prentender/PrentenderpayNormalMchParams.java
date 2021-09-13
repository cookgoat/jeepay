package com.jeequan.jeepay.core.model.params.prentender;

import lombok.Data;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.model.params.NormalMchParams;

/**
 * @author axl rose
 * @date 2021/9/13
 */
@Data
public class PrentenderpayNormalMchParams extends NormalMchParams {

  private String bizType;

  private String productType;

    @Override
    public String deSenData() {
        PrentenderpayNormalMchParams prentenderpayIsvParams = this;
        return ((JSONObject) JSON.toJSON(prentenderpayIsvParams)).toJSONString();
    }

}
