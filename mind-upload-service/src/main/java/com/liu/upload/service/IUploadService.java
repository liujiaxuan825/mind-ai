package com.liu.upload.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    String uploadFileToOss(MultipartFile file);

    String uploadImageToOss(MultipartFile file);
}
