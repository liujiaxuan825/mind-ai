package com.liu.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 菜单树 VO，返回给前端渲染侧边栏。
 * 叶子菜单 menuType=2；目录菜单 menuType=1，其 children 非空。
 * 前后端约定字段与前端 el-menu 对应：path 作为路由索引，icon 为 Element Plus 图标名。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuVO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 父菜单ID，0 为顶级
     */
    private Long parentId;

    /**
     * 菜单名称（展示）
     */
    private String menuName;

    /**
     * 路由路径（el-menu-item 的 index / 路由跳转用）
     */
    private String path;

    /**
     * 组件路径（预留）
     */
    private String component;

    /**
     * Element Plus 图标组件名，如 ChatDotRound / Folder / Setting
     */
    private String icon;

    /**
     * 1 目录  2 叶子
     */
    private Integer menuType;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 菜单对应权限码（可选，前端可据此做精细化控制）
     */
    private String permCode;

    /**
     * 子菜单（目录时非空）
     */
    private List<MenuVO> children;
}
