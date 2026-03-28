package com.liu.file.mapper;

import com.liu.file.domain.Entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-19
 */
@Mapper
public interface MindDocumentMapper extends BaseMapper<Document> {


    String selectDocFileKey(Long docId, Long userId);
}
