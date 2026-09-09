package com.liu.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.user.domain.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper。
 * 注意：菜单关联查询使用 role_ids 批量查询，对同一个用户的多个角色合并去重。
 */
@Mapper
public interface MindMenuMapper extends BaseMapper<Menu> {

    /**
     * 根据多个角色ID，查询所有启用、显示的菜单（按 sort_order 升序、id 升序）。
     * 使用 DISTINCT 防止多个角色拥有同一份菜单时重复返回。
     */
    @Select("""
            <script>
            SELECT DISTINCT m.*
              FROM mind_menu m
              INNER JOIN mind_role_menu rm ON rm.menu_id = m.id
             WHERE rm.role_id IN
                <foreach collection='roleIds' item='rid' open='(' separator=',' close=')'>#{rid}</foreach>
               AND m.status = 1
               AND m.visible = 1
             ORDER BY m.parent_id ASC, m.sort_order ASC, m.id ASC
            </script>
            """)
    List<Menu> selectMenusByRoleIds(@Param("roleIds") List<Long> roleIds);
}
