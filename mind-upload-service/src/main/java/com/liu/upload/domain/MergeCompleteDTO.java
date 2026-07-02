package com.liu.upload.domain;

import lombok.Data;

@Data
public class MergeCompleteDTO {
    private String fileMd5;
    private String fileName;
    private String objectKey;
    private String uploadId;
    private Long fileSize;
}
