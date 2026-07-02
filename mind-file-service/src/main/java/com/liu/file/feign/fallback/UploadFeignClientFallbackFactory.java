package com.liu.file.feign.fallback;

import com.liu.common.common.Result;
import com.liu.file.feign.UploadFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class UploadFeignClientFallbackFactory implements FallbackFactory<UploadFeignClient> {

    @Override
    public UploadFeignClient create(Throwable cause) {
        log.error("上传服务调用失败，触发熔断降级", cause);
        return new UploadFeignClient() {
            @Override
            public Result<String> uploadFile(MultipartFile file) {
                return Result.error("上传服务调用失败，触发熔断降级");
            }
        };
    }
}
