package com.yourname.mind.aliyun;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AliyunOssUtil {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    private final OSS ossClient;



    /**
     * 上传文件到OSS（通用方法）
     *
     * @param file Spring MVC接收的文件对象
     * @return 文件的完整访问URL
     */
    public String uploadFile(MultipartFile file) {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String objectName = generateFileName(fileExtension);

        return uploadFile(file, objectName);
    }

    /**
     * 上传文件到OSS（指定文件名）
     *
     * @param file       Spring MVC接收的文件对象
     * @param objectName 在OSS中存储的文件路径和名称
     * @return 文件的完整访问URL
     */
    public String uploadFile(MultipartFile file, String objectName) {
        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IllegalArgumentException("对象名不能为空");
        }


        try (InputStream inputStream = file.getInputStream()) {
            // 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);

            // 执行上传
            ossClient.putObject(putObjectRequest);

            // 返回文件的访问URL
            return generateFileUrl(objectName);

        } catch (OSSException | ClientException | IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除OSS中的文件
     *
     * @param objectName 在OSS中存储的文件路径和名称
     */
    public void deleteFile(String objectName) {
        if (objectName == null || objectName.trim().isEmpty()) {
            return;
        }
        try {
            ossClient.deleteObject(bucketName, objectName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成唯一文件名
     *
     * @param fileExtension 文件扩展名（包含点，如 ".jpg"）
     * @return 唯一文件名
     */
    private String generateFileName(String fileExtension) {
        return UUID.randomUUID().toString().replace("-", "") + fileExtension;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 原始文件名
     * @return 文件扩展名（如 ".jpg"）
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ""; // 没有扩展名
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 生成文件的完整访问URL
     *
     * @param objectName 在OSS中的文件名
     * @return 完整URL
     */
    private String generateFileUrl(String objectName) {
        if (urlPrefix != null && !urlPrefix.isEmpty()) {
            return urlPrefix + objectName;
        }
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }


    /**
     * 从阿里云下载文件到服务器
     *
     * @param fileKey
     * @return path对象
     */
    public Path downloadToTemp(String fileKey) throws IOException {
        // 1. 检查文件大小（限制在50MB以内）
        long fileSize = getFileSize(fileKey);
        if (fileSize > 50 * 1024 * 1024) {
            throw new IOException("文件大小超过50MB限制，当前大小: " + fileSize + " bytes");
        }

        if (fileSize <= 0) {
            throw new IOException("文件大小异常: " + fileSize);
        }

        // 2. 在服务器临时目录创建文件
        Path tempFile = Files.createTempFile("doc_parse_", ".tmp");

        try (InputStream inputStream = ossClient.getObject(bucketName, fileKey).getObjectContent();
             OutputStream outputStream = Files.newOutputStream(tempFile)) {

            // 3. 流式复制文件内容
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            log.info("文件下载完成，fileKey: {}, 大小: {} bytes", fileKey, totalBytes);
            return tempFile;

        } catch (Exception e) {
            // 4. 如果下载失败，清理临时文件
            Files.deleteIfExists(tempFile);
            throw new IOException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件大小
     */
    private long getFileSize(String fileKey) {
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, fileKey);
            return metadata.getContentLength();
        } catch (Exception e) {
            log.error("获取文件大小失败", e);
            return -1;
        }
    }
}