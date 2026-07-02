package com.liu.upload.service.Impl;

import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PartSummary;
import com.liu.common.common.Result;
import com.liu.common.untils.AliyunOssUtil;
import com.liu.common.exception.BusinessException;
import com.liu.upload.domain.ChunkSignDTO;
import com.liu.upload.domain.MergeCompleteDTO;
import com.liu.upload.domain.UploadPreCheckDTO;
import com.liu.upload.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements IUploadService {

    private final AliyunOssUtil aliyunOssUtil;

    private final UploadStatusService uploadStatusService;


    @Override
    public String uploadFileToOss(MultipartFile file) {

        validateFile(file);
        // 验证文件类型
        String contentType = file.getContentType();
        if (!isSupportedFileType(contentType)) {
            throw new BusinessException("不支持的文件格式");
        }

        return aliyunOssUtil.uploadFile(file);
    }

    @Override
    public String uploadImageToOss(MultipartFile file) {

        validateFile(file);

        // 验证文件类型
        String contentType = file.getContentType();
        if (!isImageFile(contentType)) {
            throw new BusinessException("不支持的图片文件格式");
        }

        return aliyunOssUtil.uploadFile(file);
    }

    @Override
    public Object preCheck(UploadPreCheckDTO dto) {
        String fileMd5 = dto.getFileMd5();
        String objectKey = generateObjectKey(fileMd5, dto.getFileName());

        //1.判断文件是否已经存在于oss
        if(aliyunOssUtil.isObjectExist(objectKey)){
            log.info("大文件已存在，秒传成功，文件名：{}", dto.getFileName());
            //TODO:发送mq解析
            return Result.success("秒传成功");
        }

        //2.判断是否又传了一半的文件信息
        Map<Object, Object> status = uploadStatusService.getUploadStatus(fileMd5);
        if(status != null) {
            String uploadId = (String) status.get("uploadId");
            List<PartSummary> partSummaries = aliyunOssUtil.listUploadedParts(objectKey, uploadId);
            List<Integer> uploadChunkNumbers = partSummaries.stream().map(PartSummary::getPartNumber).toList();

            log.info("已上传的文件块个数：{}", uploadChunkNumbers.size());

            return Result.success(Map.of("uploadId", uploadId, "objectKey", objectKey, "uploadChunkNumbers", uploadChunkNumbers));
        }

        String uploadId = aliyunOssUtil.initiateMultipartUpload(objectKey);
        uploadStatusService.saveUploadStatus(fileMd5, dto.getFileName(), objectKey, uploadId, dto.getFileSize(), dto.getTotalChunks());
        log.info("初始化上传任务，上传任务ID：{}", uploadId);
        return Result.success(Map.of("uploadId", uploadId,  "objectKey", objectKey, "uploadChunkNumbers", List.of()));
    }

    private String generateObjectKey(String fileMd5, String fileName) {
        //TODO
        return null;
    }

    @Override
    public Object getChunkUpload(ChunkSignDTO dto) {
        String url = aliyunOssUtil.generateChunkUploadUrl(
                dto.getObjectKey(),
                dto.getUploadId(),
                dto.getChunkNumber()
        );
        return Result.success(url);
    }

    @Override
    public Object mergeComplete(MergeCompleteDTO dto) {
        try {
            List<PartSummary> partSummaries = aliyunOssUtil.listUploadedParts(dto.getObjectKey(), dto.getUploadId());
            List<PartETag> partETags = partSummaries.stream().map(part -> new PartETag(part.getPartNumber(), part.getETag())).toList();
            aliyunOssUtil.completeMultipartUpload(dto.getObjectKey(), dto.getUploadId(), partETags);
            uploadStatusService.deleteUploadStatus(dto.getFileMd5());
            //TODO:发送mq解析
            return Result.success(dto.getObjectKey());
        } catch (Exception e) {
            log.error("合并上传失败，文件名：{}", dto.getFileName(), e);
            throw new BusinessException("大文件合并上传失败");
        }
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小 (50MB)
        long maxSize = 50 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过50MB");
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

    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }


}
