package com.jeequan.jeepay.service.biz;

import com.jeequan.jeepay.service.biz.rq.ResellerOrderImportRequest;

/**
 * @author axl rose
 * @date 2021/9/14
 */
public interface ResellerOrderImportService {

    void batchImport(ResellerOrderImportRequest resellerImportRequest);


}
