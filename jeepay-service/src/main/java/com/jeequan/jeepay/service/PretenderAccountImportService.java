package com.jeequan.jeepay.service;

import com.jeequan.jeepay.service.rq.PretenderAccountImportRequest;

public interface PretenderAccountImportService {

    void batchImport(PretenderAccountImportRequest pretenderAccountImportRequest);

}
