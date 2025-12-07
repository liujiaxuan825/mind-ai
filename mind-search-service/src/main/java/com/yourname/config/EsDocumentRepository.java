package com.yourname.config;

import com.yourname.domain.Entity.EsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface EsDocumentRepository extends ElasticsearchRepository<EsDocument, String> {
    
}
