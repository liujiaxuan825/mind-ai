package com.liu.user.service;

import com.liu.common.common.Result;
import com.liu.user.domain.vo.MenuVO;

import java.util.List;

/**
 * 菜单服务接口。
 */
public interface MindMenuService {

    /**
     * 根据当前登录用户的角色，返回菜单树（用于前端侧边栏动态渲染）。
     *
     * @return 按 sortOrder 升序组装好的两级菜单树
     */
    Result<List<MenuVO>> listCurrentUserMenus();

    /**
     * 根据用户ID，返回菜单树。
     * 登录成功时调用，避免依赖会话上下文。
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    List<MenuVO> listMenusByUserId(Long userId);
}
