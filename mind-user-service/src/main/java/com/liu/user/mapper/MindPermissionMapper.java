package com.liu.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.user.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MindPermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT p.* FROM mind_permission p INNER JOIN mind_role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = #{roleId}")
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    @Select("""
            SELECT DISTINCT p.*
            FROM mind_permission p
            INNER JOIN mind_role_permission rp ON p.id = rp.permission_id
            INNER JOIN mind_user_role ur ON rp.role_id = ur.role_id
            INNER JOIN mind_role r ON r.id = ur.role_id AND r.status = 1
            WHERE ur.user_id = #{userId} AND p.status = 1
            """)
    List<Permission> selectEnabledByUserId(@Param("userId") Long userId);
}
