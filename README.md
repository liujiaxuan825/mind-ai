# mind-ai的项目开发笔记

##基础架构
1.用户核心领域：用户、权限、知识库






##搜索功能实现
1.全局搜索,知识库界面搜索:
(1)关键词匹配
(2)指定知识库集合

##es返回的数据类型
{
"took": 5,                      // 查询耗时（毫秒）
"timed_out": false,             // 查询是否超时
"_shards": { ... },             // 分片信息
"hits": {                       // 命中文档的核心区域
"total": {                    // 匹配文档总数
"value": 1,
"relation": "eq"
},
"max_score": 1.0,             // 所有匹配中的最高相关性得分
"hits": [                     // 文档数组，默认按得分排序
{
"_index": "documents",    // 索引名
"_id": "es_auto_id_123",  // ES自动生成的文档ID
"_score": 1.0,            // 此文档的相关性得分
"_source": {              // ❗ **核心**：你索引的原始文档数据
"id": 1001,             // 你的业务ID
"knowledgeBaseId": 10,
"title": "人工智能的发展历程",
"content": "人工智能是未来科技的重要方向...",
"author": "张三",
"createTime": "2023-10-01T10:00:00"
// ... 其他你在 `DocumentIndex` 中定义的字段
}
}
]
}
}
##返回数据类型在代码中的体现
核心返回类：SearchHits<T> 与 SearchHit<T>
SearchHits<T>: 这代表一次查询的整体结果，相当于 JSON 响应中 hits 那一层。

SearchHit<T>: 这代表单个命中文档，相当于 hits.hits 数组里的一个对象。其中的泛型 T 就是你定义的实体类，例如 esDocument。

它们的关系是这样的：SearchHits<T> 包含一个 List<SearchHit<T>> 列表。

##es的api
ElasticsearchRepository 就是那个“开箱即用”的简单CRUD工具，而 ElasticsearchRestTemplate 则是你用来编写“复杂SQL”（即Elasticsearch DSL）的利器。

##es如何构建查询条件
在新版Java Client中，所有查询构建都围绕 co.elastic.clients.elasticsearch._types.query_dsl 包下的类型安全对象展开，并通过 NativeQuery.builder() 组装。
1. 基础查询构造：MultiMatchQuery、TermQuery、RangeQuery
   这些查询最终都需调用 ._toQuery() 转换为统一的 Query 类型。

多字段匹配 (MultiMatchQuery)：用于你的关键词搜索。

// 1. 创建基础查询对象
MultiMatchQuery multiMatch = MultiMatchQuery.of(m -> m // `of`是静态工厂方法，`m`是Lambda参数
.query("人工智能")            // 类型：String
.fields(List.of("title^2.0", "content", "tags^1.5")) // 类型：List<String>，`^`后跟权重[citation:5]
.type(TextQueryType.BestFields) // 类型：枚举，如 BestFields, MostFields, CrossFields[citation:5]
.tieBreaker(0.3f)            // 类型：float，用于平衡多字段得分[citation:1][citation:2]
.minimumShouldMatch("50%")   // 类型：String，最少匹配项[citation:2][citation:5]
);
// 2. 转换为统一的Query类型
Query keywordQuery = multiMatch._toQuery();
精确匹配 (TermQuery)：用于筛选知识库ID、作者等。

范围查询 (RangeQuery)：用于时间筛选。
Query filterQuery = TermQuery.of(t -> t
.field("knowledgeBaseId") // 类型：String，字段名
.value(FieldValue.of(1024L)) // 类型：FieldValue，包装各种类型的值
)._toQuery();

组合查询构造：BoolQuery
这是实现复杂逻辑（must， should， must_not， filter）的核心。

java
BoolQuery boolQuery = BoolQuery.of(b -> b
.must( // 必须满足，参与算分，如关键词查询
MultiMatchQuery.of(m -> m.query("智能").fields("title", "content"))._toQuery()
)
.filter( // 必须满足，不参与算分，性能高，用于硬性筛选[citation:6]
TermQuery.of(t -> t.field("status").value("PUBLISHED"))._toQuery()
)
.should( // 应该满足，满足则加分，如标签匹配
TermQuery.of(t -> t.field("tags").value("AI"))._toQuery()
)
.minimumShouldMatch("1") // 至少满足几个should子句
);
Query finalQuery = boolQuery._toQuery();

聚合构造 (Aggregation): 用于“全局搜索”的统计
java
// 1. 构建一个按字段分组的聚合
TermsAggregation termsAgg = TermsAggregation.of(t -> t
.field("knowledgeBaseId") // 按此字段分组
.size(100)                // 返回分组数
);
// 2. 用Spring Data ES的类包装起来
org.springframework.data.elasticsearch.client.elc.Aggregation aggregation =
new org.springframework.data.elasticsearch.client.elc.Aggregation(termsAgg._toAggregation());

// 3. 在构建NativeQuery时加入
NativeQuery query = NativeQuery.builder()
.withQuery(yourBaseQuery)
.withAggregation("group_by_kb", aggregation) // “group_by_kb”是自定义聚合名
.build();

##缓存设计策略
1.对于本项目的缓存,重点放在知识库页面的基本信息分页查询和文档页面的基本信息分页查询两个方面
2.分析业务,对于这几个业务来说,整体属于"读多写少"的操作,对于查询，我们首先尝试从缓存中获取，如果缓存中存在则直接返回；如果不存在，则查询数据库，并将结果存入缓存。
当数据发生更新（增、删、改）时，我们需要更新或删除缓存，以保证数据一致性。
3.对于分页查询的缓存来说,首先要进行单个内容的缓存,所以文档的基本信息和知识库基本信息进行缓存实现