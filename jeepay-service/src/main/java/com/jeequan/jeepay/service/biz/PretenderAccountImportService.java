package com.jeequan.jeepay.service.biz;

import com.jeequan.jeepay.service.biz.rq.PretenderAccountImportRequest;

public interface PretenderAccountImportService {

    void batchImport(PretenderAccountImportRequest pretenderAccountImportRequest);

}
