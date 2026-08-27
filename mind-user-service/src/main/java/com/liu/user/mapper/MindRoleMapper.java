package com.liu.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.user.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MindRoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.* FROM mind_role r INNER JOIN mind_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<Role> selectRolesByUserId(@Param("userId") Long userId);
}
