package com.yourname.service.Impl;

import com.yourname.mind.aliyun.AliyunOssUtil;
import com.yourname.mind.exception.BusinessException;
import com.yourname.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements IUploadService {

    private final AliyunOssUtil aliyunOssUtil;


    @Override
    public String uploadFileToOss(MultipartFile file) {

        validateFile(file);

        return aliyunOssUtil.uploadFile(file);
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小 (100MB)
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过100MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (!isSupportedFileType(contentType)) {
            throw new BusinessException("不支持的文件格式");
        }
    }

    private boolean isSupportedFileType(String contentType) {
        return contentType != null && (
                contentType.startsWith("application/pdf") ||
                        contentType.startsWith("application/msword") ||
                        contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                        contentType.startsWith("text/") ||
                        contentType.equals("application/vnd.ms-excel") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/vnd.ms-powerpoint") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
        );
    }




}
