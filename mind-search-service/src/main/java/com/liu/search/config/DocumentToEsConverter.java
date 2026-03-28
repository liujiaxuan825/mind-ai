package com.liu.search.config;

import com.liu.common.untils.UserContext;
import com.liu.file.domain.Entity.Document;
import com.liu.search.domain.Entity.EsDocument;
import org.springframework.stereotype.Component;

@Component
public class DocumentToEsConverter {

    public EsDocument convertDocumentToEsDocument(Document document) {
        EsDocument esDocument = new EsDocument();
        esDocument.setId(document.getId());
        esDocument.setTitle(document.getName());
        //TODO: 以后共享知识库需要调整
        esDocument.setAuthor(UserContext.getUsername());
        esDocument.setKnowledgeId(document.getKnowledgeId());
        esDocument.setContent(document.getContentText());
        esDocument.setCreateTime(document.getCreatedTime());
        esDocument.setUpdateTime(document.getUpdatedTime());
        //TODO: summary需要ai进行解析
        esDocument.setSummary(null);
        return esDocument;
    }
}
