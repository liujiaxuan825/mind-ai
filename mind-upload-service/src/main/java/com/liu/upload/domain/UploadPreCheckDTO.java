package com.liu.upload.domain;

import lombok.Data;

@Data
public class UploadPreCheckDTO {

    /**
     * 文件md5值
     */
    private String fileMd5;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 总块数
     */
    private Integer totalChunks;
}
