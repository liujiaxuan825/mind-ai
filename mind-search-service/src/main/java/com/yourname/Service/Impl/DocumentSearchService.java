package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drew.lang.StringUtil;
import com.yourname.Service.IDocumentSearchService;
import com.yourname.Service.IMindDocumentService;
import com.yourname.Service.IMindKnowledgeService;
import com.yourname.domain.DTO.DocSearchDTO;
import com.yourname.domain.DTO.GlobalSearchDTO;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.Entity.EsDocument;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.domain.VO.EsDocumentSearchVO;
import com.yourname.domain.VO.GlobalSearchResultVO;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentSearchService implements IDocumentSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final IMindKnowledgeService knowledgeService;

    @Override
    public Result<PageResultVO<GlobalSearchResultVO>> search(GlobalSearchDTO dto, PageRequestDTO page) {
        String keyWord = dto.getKeyWord();
        String filter = dto.getFilter();

        TermsAggregation terms = TermsAggregation.of(t -> t.field("knowledgeId").size(100));
        Aggregation aggregation = terms._toAggregation();

        NativeQuery resultQuery = NativeQuery.builder()
                .withQuery(BoolQuery.of(b->b.must(MultiMatchQuery.of(m->m.query(keyWord).fields("content","title"))._toQuery())
                        .must(TermQuery.of(t->t.field("filter").value(filter))._toQuery()))._toQuery())
                .withAggregation("group_by_kbId", aggregation)
                .build();

        SearchHits<EsDocument> searchHits = elasticsearchTemplate.search(resultQuery, EsDocument.class);

        AggregationsContainer<?> aggregations = searchHits.getAggregations();
        Map<String, Aggregate> aggregationMap = (Map<String, Aggregate>) aggregations.aggregations();
        Aggregate aggregate = aggregationMap.get("group_by_kbId");
        if (aggregate == null) {
            return Result.success(new PageResultVO<>());
        }
        if (!aggregate.isLterms()) {
            return Result.success(new PageResultVO<>());
        }
        LongTermsAggregate lterms = aggregate.lterms();
        List<LongTermsBucket> array = lterms.buckets().array();
        List<GlobalSearchResultVO> voList = array.stream().map(b -> {
            GlobalSearchResultVO vo = new GlobalSearchResultVO();
            vo.setRelatedCount(b.docCount());
            KnowledgeVO know = new KnowledgeVO();
            know.setId(b.key());
            vo.setKnowledgeVO(know);
            return vo;
        }).collect(Collectors.toList());

        List<Long> kdIdList = voList.stream().map(vo -> vo.getKnowledgeVO().getId()).collect(Collectors.toList());

        if (kdIdList.isEmpty()) {
            return Result.success(new PageResultVO<>());
        }
        //TODO:对于这个查询知识库的基本信息,将来优化为缓存实现
        List<Knowledge> knowledgeList = knowledgeService.list(new LambdaQueryWrapper<Knowledge>().in(Knowledge::getId, kdIdList));
        Map<Long, KnowledgeVO> collect = knowledgeList.stream()
                .collect(Collectors.toMap(
                        Knowledge::getId,
                        knowledge -> BeanUtil.copyProperties(knowledge, KnowledgeVO.class),
                        (existing, replacement) -> existing
                ));

        List<GlobalSearchResultVO> list = combineCountAndInf(collect,voList);

        return manualPagination(list, page);

    }

    @Override
    public Result<PageResultVO<EsDocumentSearchVO>> docSearch(DocSearchDTO dto, PageRequestDTO page) {
        Long knowledgeId = dto.getKnowledgeId();
        String keyWord = dto.getKeyWord();

        HighlightParameters highlightParameters = HighlightParameters.builder()
                .withPreTags("<em class=\"highlight\">")
                .withPostTags("</em>")
                .withFragmentSize(150)
                .withNumberOfFragments(3)
                .build();
        HighlightFieldParameters title = HighlightFieldParameters.builder()
                .withFragmentSize(50)
                .withNumberOfFragments(1)
                .build();
        HighlightFieldParameters content = HighlightFieldParameters.builder().build();
        List<HighlightField> list = Arrays.asList(new HighlightField("title", title),
                new HighlightField("content", content));
        Highlight highlight = new Highlight(highlightParameters,list);
        HighlightQuery highlightQuery = new HighlightQuery(highlight,EsDocument.class);

        NativeQuery result = NativeQuery.builder()
                .withQuery(BoolQuery.of(b->b.must(MultiMatchQuery.of(m->m.query(keyWord).fields("content","title"))._toQuery())
                        .must(TermQuery.of(t->t.field("knowledgeId").value(knowledgeId))._toQuery()))._toQuery())
                .withPageable(page.convertToPageable())
                .withHighlightQuery(highlightQuery)
                .build();

        SearchHits<EsDocument> searchHits = elasticsearchTemplate.search(result, EsDocument.class);

        List<EsDocumentSearchVO> searchList = new ArrayList<>();
        List<SearchHit<EsDocument>> hits = searchHits.getSearchHits();
        for (SearchHit<EsDocument> hit : hits) {
            EsDocumentSearchVO eds = new EsDocumentSearchVO();

            EsDocument es = hit.getContent();
            BeanUtil.copyProperties(es,eds.getEsDocumentVO());

            eds.setScore(hit.getScore());

            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            eds.setHighlightTitle(highlightFields.get("title"));
            eds.setHighlightContent(highlightFields.get("content"));
        }
        PageResultVO<EsDocumentSearchVO> lastResult = PageResultVO.success(searchList, searchHits.getTotalHits(), page);

        return Result.success(lastResult);
    }




    private Result<PageResultVO<GlobalSearchResultVO>> manualPagination(List<GlobalSearchResultVO> voList, PageRequestDTO page) {
        int pageNum = page.getPageNum(); // 假设从0开始
        int pageSize = page.getPageSize();
        int totalItems = voList.size();

        // 计算分页起始索引
        int startIndex = pageNum * pageSize;
        if (startIndex >= totalItems) {
            // 请求的页码超出范围，返回空页
            return Result.success(PageResultVO.empty(page));
        }
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        // 截取当前页的子列表 (注意：subList返回的是视图，若需独立列表可new ArrayList包裹)
        List<GlobalSearchResultVO> pagedList = voList.subList(startIndex, endIndex);
        // 构建分页结果VO
        PageResultVO<GlobalSearchResultVO> pageResult = PageResultVO.success(pagedList, (long) totalItems, page);
        return Result.success(pageResult);
    }

    private List<GlobalSearchResultVO> combineCountAndInf(Map<Long,KnowledgeVO> map, List<GlobalSearchResultVO> voList) {
        for (GlobalSearchResultVO vo : voList) {
            Long id = vo.getKnowledgeVO().getId();
            KnowledgeVO knowledge = map.get(id);
            if (knowledge != null) {
                vo.setKnowledgeVO(knowledge);
            }
        }
        return voList;
    }
}
