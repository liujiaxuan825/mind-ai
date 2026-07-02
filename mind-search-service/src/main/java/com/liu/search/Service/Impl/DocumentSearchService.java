package com.liu.search.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.liu.common.common.domain.ComKnowledgeVO;
import com.liu.common.untils.UserContext;
import com.liu.search.Service.IDocumentSearchService;
import com.liu.search.domain.DTO.DocSearchDTO;
import com.liu.search.domain.DTO.GlobalSearchDTO;
import com.liu.search.domain.Entity.EsDocument;
import com.liu.search.domain.VO.EsDocumentSearchVO;
import com.liu.search.domain.VO.EsDocumentVO;
import com.liu.search.domain.VO.GlobalSearchResultVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import com.liu.search.feign.KnowledgeFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentSearchService implements IDocumentSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    private final KnowledgeFeignClient knowledgeFeignClient;


    @Override
    public Result<PageResultVO<GlobalSearchResultVO>> search(GlobalSearchDTO dto) {
        String keyWord = dto.getKeyWord();
        TermsAggregation terms = TermsAggregation.of(t -> t.field("knowledgeId").size(100));
        Aggregation aggregation = terms._toAggregation();

        NativeQuery resultQuery = NativeQuery.builder()
                .withQuery(BoolQuery.of(b -> b
                        .must(MultiMatchQuery.of(m -> m.query(keyWord).fields("content", "title"))._toQuery())
                        .filter(TermQuery.of(t -> t
                                .field("author")
                                .value(UserContext.getUserId())
                        )._toQuery())
                )._toQuery())
                .withAggregation("group_by_kbId", aggregation)
                .withMaxResults(0)
                .build();

        SearchHits<EsDocument> searchHits = elasticsearchTemplate.search(resultQuery, EsDocument.class);


        AggregationsContainer<?> aggregations = searchHits.getAggregations();
        if (aggregations == null) {
            return Result.success(new PageResultVO<>());
        }

        List<ElasticsearchAggregation> aggregateList = (List<ElasticsearchAggregation>) aggregations.aggregations();

        if (CollectionUtils.isEmpty(aggregateList)) {
            return Result.success(new PageResultVO<>());
        }

        ElasticsearchAggregation esAgg = aggregateList.get(0);
        Aggregate aggregate = esAgg.aggregation().getAggregate();
        if (aggregate == null || !aggregate.isLterms()) {
            return Result.success(new PageResultVO<>());
        }

        LongTermsAggregate lterms = aggregate.lterms();
        List<LongTermsBucket> array = lterms.buckets().array();
        List<GlobalSearchResultVO> voList = array.stream().map(b -> {
            GlobalSearchResultVO vo = new GlobalSearchResultVO();
            vo.setRelatedCount(b.docCount());
            ComKnowledgeVO know = new ComKnowledgeVO();
            know.setId(b.key());
            vo.setKnowledgeVO(know);
            return vo;
        }).collect(Collectors.toList());

        List<Long> kdIdList = voList.stream().map(vo -> vo.getKnowledgeVO().getId()).collect(Collectors.toList());

        if (kdIdList.isEmpty()) {
            return Result.success(new PageResultVO<>());
        }
        List<ComKnowledgeVO> knowledgeList = knowledgeFeignClient.list(kdIdList);
        Map<Long, ComKnowledgeVO> collect = knowledgeList.stream()
                .collect(Collectors.toMap(
                        ComKnowledgeVO::getId,
                        knowledgeVO -> knowledgeVO
                ));

        List<GlobalSearchResultVO> list = combineCountAndInf(collect, voList);

        return manualPagination(list, dto.getPage());

    }

    @Override
    public Result<PageResultVO<EsDocumentSearchVO>> docSearch(DocSearchDTO dto) {
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
        Highlight highlight = new Highlight(highlightParameters, list);
        HighlightQuery highlightQuery = new HighlightQuery(highlight, EsDocument.class);

        NativeQuery result = NativeQuery.builder()
                .withQuery(BoolQuery.of(b -> b
                        .must(MultiMatchQuery.of(m -> m.query(keyWord).fields("content", "title"))._toQuery())
                        .filter(TermQuery.of(t -> t.field("knowledgeId").value(knowledgeId))._toQuery())
                        .filter(TermQuery.of(t -> t.field("author").value(UserContext.getUserId()))._toQuery()))
                        ._toQuery())
                .withPageable(dto.getPage().convertToPageable())
                .withHighlightQuery(highlightQuery)
                .build();

        SearchHits<EsDocument> searchHits = elasticsearchTemplate.search(result, EsDocument.class);

        List<EsDocumentSearchVO> searchList = new ArrayList<>();
        List<SearchHit<EsDocument>> hits = searchHits.getSearchHits();
        for (SearchHit<EsDocument> hit : hits) {
            EsDocumentSearchVO eds = new EsDocumentSearchVO();

            EsDocument es = hit.getContent();
            EsDocumentVO esDocumentVO = BeanUtil.copyProperties(es, EsDocumentVO.class);
            eds.setEsDocumentVO(esDocumentVO);
            eds.setScore(hit.getScore());

            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            eds.setHighlightTitle(highlightFields.get("title"));
            eds.setHighlightContent(highlightFields.get("content"));
            searchList.add(eds);
        }
        PageResultVO<EsDocumentSearchVO> lastResult = PageResultVO.success(searchList, searchHits.getTotalHits(), dto.getPage());

        return Result.success(lastResult);
    }

    /*@Override
    public Result<EsDocumentSearchVO> singleSearch(SingleSearchDTO dto) {
        Long documentId = dto.getDocumentId();
        String keyWord = dto.getKeyWord();
        HighlightParameters highlightParameters = HighlightParameters.builder()
                .withFragmentSize(150)
                .withNumberOfFragments(1)
                .build();
        HighlightFieldParameters title = HighlightFieldParameters.builder()
                .withFragmentSize(20)
                .withNumberOfFragments(1)
                .build();
        HighlightFieldParameters content = HighlightFieldParameters.builder().build();
        List<HighlightField> list = Arrays.asList(new HighlightField("title", title), new HighlightField("content", content));
        Highlight highlight = new Highlight(highlightParameters, list);
        HighlightQuery highlightQuery = new HighlightQuery(highlight, EsDocument.class);
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(BoolQuery.of(b -> b
                        .must(MultiMatchQuery.of(m -> m.fields("content", "title").query(keyWord))._toQuery())
                        .filter(TermQuery.of(t -> t.field("documentId").value(documentId))._toQuery()))._toQuery()
                )
                .withHighlightQuery(highlightQuery)
                .build();
        SearchHits<EsDocument> searchResult = elasticsearchTemplate.search(nativeQuery, EsDocument.class);
        List<SearchHit<EsDocument>> searchHits = searchResult.getSearchHits();
        if (searchHits.isEmpty()) {
            return Result.success();
        }

        EsDocumentSearchVO result = new EsDocumentSearchVO();
        SearchHit<EsDocument> esDocumentSearchHit = searchHits.getFirst();
        EsDocument esDocument = esDocumentSearchHit.getContent();
        EsDocumentVO esDocumentVO = BeanUtil.copyProperties(esDocument, EsDocumentVO.class);
        result.setEsDocumentVO(esDocumentVO);

        result.setScore(esDocumentSearchHit.getScore());

        Map<String, List<String>> highlightFields = esDocumentSearchHit.getHighlightFields();
        result.setHighlightTitle(highlightFields.get("title"));
        result.setHighlightContent(highlightFields.get("content"));

        return Result.success(result);
    }*/


    private Result<PageResultVO<GlobalSearchResultVO>> manualPagination(List<GlobalSearchResultVO> voList, PageRequestDTO page) {
        int pageNum = page.getPageNum() - 1; // 关键！！！前端页码从1开始，你必须-1！！！
        int pageSize = page.getPageSize();
        int totalItems = voList.size();

        int startIndex = pageNum * pageSize;

        // 修复：不调用 empty()，自己构造空页
        if (startIndex >= totalItems) {
            PageResultVO<GlobalSearchResultVO> emptyPage = new PageResultVO<>();
            emptyPage.setList(new ArrayList<>());
            emptyPage.setTotal(0L);
            return Result.success(emptyPage);
        }

        int endIndex = Math.min(startIndex + pageSize, totalItems);
        List<GlobalSearchResultVO> pagedList = voList.subList(startIndex, endIndex);

        PageResultVO<GlobalSearchResultVO> pageResult = PageResultVO.success(pagedList, (long) totalItems, page);
        return Result.success(pageResult);
    }

    private List<GlobalSearchResultVO> combineCountAndInf(Map<Long, ComKnowledgeVO> map, List<GlobalSearchResultVO> voList) {
        for (GlobalSearchResultVO vo : voList) {
            Long id = vo.getKnowledgeVO().getId();
            ComKnowledgeVO knowledge = map.get(id);
            if (knowledge != null) {
                vo.setKnowledgeVO(knowledge);
            }
        }
        return voList;
    }
}
