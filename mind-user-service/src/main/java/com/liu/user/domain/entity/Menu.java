package com.liu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体，对应数据库 mind_menu 表。
 * 用于前端动态侧边栏渲染，支持两级：目录(menu_type=1) + 叶子(menu_type=2)。
 *
 * @TableName mind_menu
 */
@TableName("mind_menu")
@Data
public class Menu {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父菜单ID，0 表示顶级菜单
     */
    private Long parentId;

    /**
     * 菜单名称，前端展示
     */
    private String menuName;

    /**
     * 前端路由路径，如 chat / knowledge / admin / admin/role
     */
    private String path;

    /**
     * 前端组件（预留；当前路由已静态注册，未来可用于懒加载组件路径）
     */
    private String component;

    /**
     * 图标名，对应 Element Plus @element-plus/icons-vue 中的组件名
     */
    private String icon;

    /**
     * 1 目录/父菜单  2 叶子菜单
     */
    private Integer menuType;

    /**
     * 排序（越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 菜单对应的权限码（可选），如 user:manage
     */
    private String permCode;

    /**
     * 1 启用 0 停用
     */
    private Integer status;

    /**
     * 1 显示 0 隐藏
     */
    private Integer visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
