package com.liu.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.common.Result;
import com.liu.common.common.domain.UserInfo;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.exception.BusinessException;
import com.liu.user.domain.entity.Menu;
import com.liu.user.domain.entity.Role;
import com.liu.user.domain.vo.MenuVO;
import com.liu.user.mapper.MindMenuMapper;
import com.liu.user.mapper.MindRoleMapper;
import com.liu.user.service.MindMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现。
 * 加载链路：用户ID → 角色ID列表 → 菜单列表（去重） → 组装成两级菜单树。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MindMenuServiceImpl
        extends ServiceImpl<MindMenuMapper, Menu>
        implements MindMenuService {

    private final MindRoleMapper mindRoleMapper;

    @Override
    public Result<List<MenuVO>> listCurrentUserMenus() {
        // 从 Sa-Token session 里取当前用户身份（登录时已经写入）
        Object raw = StpUtil.getSession().get(RedisConstant.userInfoKey);
        if (!(raw instanceof UserInfo userInfo)) {
            throw new BusinessException("登录状态已失效，请重新登录");
        }
        List<MenuVO> menus = listMenusByUserId(userInfo.getId());
        return Result.success(menus);
    }

    @Override
    public List<MenuVO> listMenusByUserId(Long userId) {
        // 1. 查用户的启用角色
        List<Role> roles = mindRoleMapper.selectRolesByUserId(userId);
        List<Long> roleIds = roles == null ? Collections.emptyList() : roles.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .map(Role::getId)
                .collect(Collectors.toList());

        // 无角色 => 返回空菜单（极端情况）
        if (CollectionUtils.isEmpty(roleIds)) {
            log.warn("[动态菜单] 用户 {} 未分配任何启用角色，返回空菜单", userId);
            return Collections.emptyList();
        }

        // 2. 查去重、启用、可见的菜单
        List<Menu> allMenus = baseMapper.selectMenusByRoleIds(roleIds);
        if (CollectionUtils.isEmpty(allMenus)) {
            return Collections.emptyList();
        }

        // 3. 转 VO
        List<MenuVO> allVOs = allMenus.stream().map(this::toVO).collect(Collectors.toList());

        // 4. 组装成两级树（parentId=0 作为顶层；目录菜单的子菜单挂在 children）
        return buildTwoLevelTree(allVOs);
    }

    // ------------------------------------------------------------------
    // 私有方法
    // ------------------------------------------------------------------

    private MenuVO toVO(Menu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }

    /**
     * 将扁平菜单列表组装成两级结构：顶层(parentId=0) → 其子菜单。
     * 按 menu.sortOrder 升序、id 升序已在 SQL 层保证，此处保持原序即可。
     */
    private List<MenuVO> buildTwoLevelTree(List<MenuVO> allVOs) {
        // key = parentId  value = 子菜单列表
        Map<Long, List<MenuVO>> childrenMap = new HashMap<>();
        List<MenuVO> roots = new ArrayList<>();

        for (MenuVO vo : allVOs) {
            Long parentId = vo.getParentId() == null ? 0L : vo.getParentId();
            if (parentId == 0L) {
                roots.add(vo);
            } else {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(vo);
            }
        }

        // 给父级目录挂 children（只挂存在子菜单的，避免返回空 children 列表的噪音）
        for (MenuVO root : roots) {
            List<MenuVO> children = childrenMap.get(root.getId());
            if (!CollectionUtils.isEmpty(children)) {
                root.setChildren(children);
            }
        }

        // 顶层按 sortOrder 升序（SQL 已按 parent+sort 排序，但根节点可能混在同批数据里，这里再兜底一次）
        roots.sort(Comparator
                .comparingInt((MenuVO m) -> m.getSortOrder() == null ? 0 : m.getSortOrder())
                .thenComparingLong(MenuVO::getId));
        return roots;
    }
}
