package com.liu.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.user.domain.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联 Mapper。
 * 基础 CRUD 由 MyBatis-Plus 提供；复杂授权操作可后续在 MindRbacService 中封装。
 */
@Mapper
public interface MindRoleMenuMapper extends BaseMapper<RoleMenu> {
}
