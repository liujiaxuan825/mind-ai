package com.liu.upload.service.Impl;

import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import com.liu.upload.service.IUploadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadStatusService implements IUploadStatusService {

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;

    private static final int EXPIRE_SECONDS = 24*60*60;

    private static final String UPLOAD_STATUS_KEY = "UPLOAD_STATUS_KEY";
    @Override
    public void saveUploadStatus(String fileMd5, String fileName, String objectKey, String uploadId, Long fileSize, Integer totalChunks) {
        String key = UPLOAD_STATUS_KEY + fileMd5;

        Map<String, String> status = new HashMap<>();
        status.put("fileName", fileName);
        status.put("objectKey", objectKey);
        status.put("uploadId", uploadId);
        status.put("fileSize", String.valueOf(fileSize));
        status.put("totalChunks", String.valueOf(totalChunks));

        redisCacheUtils.setWithRandomExpire(key, status, EXPIRE_SECONDS);
    }

    @Override
    public Map<Object, Object> getUploadStatus(String fileMd5) {
        String key = UPLOAD_STATUS_KEY + fileMd5;
        return redisCacheUtils.get(key, Map.class);
    }

    @Override
    public void deleteUploadStatus(String fileMd5) {
        String key = UPLOAD_STATUS_KEY + fileMd5;
        redisCacheUtils.delete(key);
    }
}
