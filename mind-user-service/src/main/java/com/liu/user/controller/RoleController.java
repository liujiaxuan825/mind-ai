package com.liu.user.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.liu.common.common.Result;
import com.liu.user.common.RbacConstant;
import com.liu.user.domain.dto.RoleDTO;
import com.liu.user.domain.vo.RoleVO;
import com.liu.user.service.MindRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/role")
@RequiredArgsConstructor
@SaCheckRole(RbacConstant.ADMIN_ROLE_CODE)
public class RoleController {

    private final MindRoleService mindRoleService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody RoleDTO roleDTO) {
        return mindRoleService.add(roleDTO);
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody RoleDTO roleDTO) {
        return mindRoleService.updateRole(roleDTO);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return mindRoleService.deleteRole(id);
    }

    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        return mindRoleService.listAll();
    }
}
