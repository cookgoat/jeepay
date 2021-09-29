package com.jeequan.jeepay.service.biz.rq;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author axl rose
 * @date 2021/9/14
 */
@Data
public class ResellerOrderImportRequest {
    private String productType;
    private Long currentUserId;
    private MultipartFile multipartFile;
}
