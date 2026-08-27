package com.liu.user.controller;

import com.liu.common.common.Result;
import com.liu.user.domain.dto.PermissionDTO;
import com.liu.user.domain.vo.PermissionVO;
import com.liu.user.service.MindPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final MindPermissionService mindPermissionService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody PermissionDTO permissionDTO) {
        return mindPermissionService.add(permissionDTO);
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody PermissionDTO permissionDTO) {
        return mindPermissionService.updatePermission(permissionDTO);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return mindPermissionService.deletePermission(id);
    }

    @GetMapping("/list")
    public Result<List<PermissionVO>> list() {
        return mindPermissionService.listAll();
    }
}
