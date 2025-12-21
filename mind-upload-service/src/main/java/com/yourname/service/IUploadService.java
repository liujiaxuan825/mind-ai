package com.yourname.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    String uploadFileToOss(MultipartFile file);

}
