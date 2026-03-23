package com.liu.file.feign;


import com.liu.common.mind.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "mind-upload-service")
public interface UploadFeignClient {

    @PostMapping("/upload/file")
    Result<Void> uploadFile(MultipartFile file);
}
