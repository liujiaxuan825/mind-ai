package com.liu.upload.domain;

import lombok.Data;

@Data
public class ChunkSignDTO {
    private String uploadId;
    private String objectKey;
    private Integer chunkNumber;
}
