package com.liu.upload.service;

import com.liu.upload.domain.ChunkSignDTO;
import com.liu.upload.domain.MergeCompleteDTO;
import com.liu.upload.domain.UploadPreCheckDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    String uploadFileToOss(MultipartFile file);

    String uploadImageToOss(MultipartFile file);

    Object preCheck(UploadPreCheckDTO dto);

    Object getChunkUpload(ChunkSignDTO dto);

    Object mergeComplete(MergeCompleteDTO dto);
}
