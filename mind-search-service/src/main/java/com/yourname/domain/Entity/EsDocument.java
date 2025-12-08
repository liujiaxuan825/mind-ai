package com.yourname.domain.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Elasticsearch 文档实体类
 * 对应索引：knowledge_docs (你的知识库文档索引)
 * 核心职责：定义Java对象与ES索引之间的映射规则
 */
@Data
@Document(indexName = "knowledge_docs")
@Setting(settingPath = "/es-settings/ik-pinyin-setting.json")
public class EsDocument {
    /**
     *唯一标识
     */
    @Id
    private Long id;

    /**
     * 数据库中文件的id
     */
    private Long documentId;

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
     *创建时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

    /**
     *最近一次更新时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime updateTime;

    /**
     * ai分析出的文章的大纲
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String summary;
}
