package com.yourname.service;

import com.yourname.domain.Entity.Document;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    String uploadFileToOss(MultipartFile file);

}
