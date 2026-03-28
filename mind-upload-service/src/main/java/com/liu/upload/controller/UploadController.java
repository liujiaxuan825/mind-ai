package com.liu.upload.controller;

import com.liu.common.common.Result;
import com.liu.upload.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final IUploadService uploadService;

    @PostMapping("/file")
    public Result<String> uploadFile(@RequestPart("file") MultipartFile file) {
        return Result.success(uploadService.uploadFileToOss(file));
    }


    @PostMapping("/image")
    public Result<String> uploadImage(MultipartFile file) {
        return Result.success(uploadService.uploadImageToOss(file));
    }

   }
