package com.liu.search.domain.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.time.LocalDate;

/**
 * Elasticsearch 文档实体类
 * 对应索引：document_docs (你的文档索引)
 * 核心职责：定义Java对象与ES索引之间的映射规则
 */
@Data
@Document(indexName = "document_docs",createIndex = false)
@Setting(settingPath = "/es-settings/ik-pinyin-setting.json")
public class EsDocument {
    /**
     *唯一标识
     */
    @Id
    private String id;

    /**
     *文档标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_pinyin_analyzer", searchAnalyzer = "ik_smart")
    private String title;

    /**
     *解析出的文档内容
     */
    @Field(type = FieldType.Text , analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 原始文档id
     */
    @Field(type = FieldType.Keyword)
    private String originalDocId;
    /**
     *文档作者
     */
    @Field(type = FieldType.Keyword)
    private String author;

    /**
     *知识库id
     */
    @Field(type = FieldType.Long)
    private Long knowledgeId;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate createTime;

    /**
     * 最近一次更新时间
     */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private LocalDate updateTime;



    /**
     * ai分析出的文章的大纲
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String summary;
}
