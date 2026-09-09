package com.liu.user.controller;

import com.liu.common.common.Result;
import com.liu.user.domain.vo.MenuVO;
import com.liu.user.service.MindMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单接口。
 * 说明：菜单权限由后端基于登录用户的角色过滤后返回，前端只负责渲染，不再写死管理员分支。
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@RefreshScope
public class MenuController {

    private final MindMenuService mindMenuService;

    /**
     * 获取当前登录用户的动态菜单树（侧边栏使用）。
     * - 普通用户(USER)：AI对话 / 知识库 / 全局搜索 / 个人中心
     * - 管理员(ADMIN)：多返回【系统管理】目录及其 3 个子菜单（角色/权限/RBAC）
     */
    @GetMapping("/mine")
    public Result<List<MenuVO>> getMyMenus() {
        return mindMenuService.listCurrentUserMenus();
    }
}
