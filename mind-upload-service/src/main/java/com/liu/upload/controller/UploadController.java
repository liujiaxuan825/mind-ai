package com.liu.upload.controller;

import com.liu.common.common.Result;
import com.liu.upload.domain.ChunkSignDTO;
import com.liu.upload.domain.MergeCompleteDTO;
import com.liu.upload.domain.UploadPreCheckDTO;
import com.liu.upload.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@RefreshScope
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

    //前端未完成的大文件分片上传三个接口
    @PostMapping("/check")
    public Result<?> preCheck(@RequestBody UploadPreCheckDTO dto) {
        return Result.success(uploadService.preCheck(dto));
    }

    @PostMapping("/chunkSign")
    public Result<?> getChunkUploadSign(@RequestBody ChunkSignDTO dto) {
        return Result.success(uploadService.getChunkUpload(dto));
    }

    @PostMapping("/mergeComplete")
    public Result<?> mergeComplete(@RequestBody MergeCompleteDTO dto) {
        return Result.success(uploadService.mergeComplete(dto));
    }
}