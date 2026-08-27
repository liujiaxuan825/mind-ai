package com.liu.user.controller;

import com.liu.common.common.Result;
import com.liu.user.domain.dto.AssignPermissionDTO;
import com.liu.user.domain.dto.AssignRoleDTO;
import com.liu.user.domain.vo.PermissionVO;
import com.liu.user.domain.vo.RoleVO;
import com.liu.user.service.MindRbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final MindRbacService mindRbacService;

    /**
     * 给用户分配角色（覆盖写入）
     */
    @PostMapping("/assign-roles")
    public Result<Void> assignRoles(@RequestBody AssignRoleDTO dto) {
        return mindRbacService.assignRoles(dto);
    }

    /**
     * 给角色分配权限（覆盖写入）
     */
    @PostMapping("/assign-permissions")
    public Result<Void> assignPermissions(@RequestBody AssignPermissionDTO dto) {
        return mindRbacService.assignPermissions(dto);
    }

    @GetMapping("/user/{userId}/roles")
    public Result<List<RoleVO>> listRolesByUserId(@PathVariable Long userId) {
        return mindRbacService.listRolesByUserId(userId);
    }

    @GetMapping("/role/{roleId}/permissions")
    public Result<List<PermissionVO>> listPermissionsByRoleId(@PathVariable Long roleId) {
        return mindRbacService.listPermissionsByRoleId(roleId);
    }

    @GetMapping("/me/permissions")
    public Result<List<PermissionVO>> listMyPermissions() {
        return mindRbacService.listMyPermissions();
    }
}
