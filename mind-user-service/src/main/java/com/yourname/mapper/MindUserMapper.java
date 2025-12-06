package com.yourname.mapper;

import com.yourname.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author liujiaxuan
* @description 针对表【mind_user】的数据库操作Mapper
* @createDate 2025-11-17 12:51:41
* @Entity com.yourname.domain.entity.MindUser
*/
@Mapper
public interface MindUserMapper extends BaseMapper<User> {

}




