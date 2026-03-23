package com.liu.search.config;

import com.liu.search.domain.Entity.EsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface EsDocumentRepository extends ElasticsearchRepository<EsDocument, String> {
    
}
