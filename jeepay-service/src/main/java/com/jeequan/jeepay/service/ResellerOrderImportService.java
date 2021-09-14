package com.jeequan.jeepay.service;

import com.jeequan.jeepay.service.rq.ResellerOrderImportRequest;

/**
 * @author axl rose
 * @date 2021/9/14
 */
public interface ResellerOrderImportService {

    void batchImport(ResellerOrderImportRequest resellerImportRequest);


}
