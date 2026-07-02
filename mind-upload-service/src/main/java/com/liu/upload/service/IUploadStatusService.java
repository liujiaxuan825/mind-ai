package com.liu.upload.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IUploadStatusService {
    void saveUploadStatus(String fileMd5, String fileName, String objectKey, String uploadId, Long fileSize, Integer totalChunks);

    Map<Object,Object> getUploadStatus(String fileMd5);

    void deleteUploadStatus(String fileMd5);
}
