package com.liu.upload.controller;

import com.liu.common.mind.common.Result;
import com.liu.upload.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final IUploadService uploadService;

    @PostMapping("/file")
    public Result<String> uploadFile(MultipartFile file) {
        return Result.success(uploadService.uploadFileToOss(file));
    }


    @PostMapping("/image")
    public Result<String> uploadImage(MultipartFile file) {
        return Result.success(uploadService.uploadImageToOss(file));
    }

   }
