package com.liu.file.feign;


import com.liu.common.common.Result;
import com.liu.file.feign.fallback.UploadFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "mind-upload-service", fallbackFactory = UploadFeignClientFallbackFactory.class)
public interface UploadFeignClient {

    @PostMapping(value = "/api/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result<String> uploadFile(@RequestPart("file") MultipartFile file);
}