package com.liu.file.provideData;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.file.Service.IMindDocumentService;
import com.liu.file.Service.IMindKnowledgeService;
import com.liu.file.domain.Entity.Document;
import com.liu.file.domain.Entity.Knowledge;
import com.liu.common.infer.BloomDataProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("bloomData")
public class BloomProvideData implements BloomDataProvider<Long> {

    @Resource
    @Lazy
    private IMindKnowledgeService mindKnowledgeService;

    @Resource
    @Lazy
    private IMindDocumentService mindDocumentService;

    @Override
    public List<Long> getAllKnowIds() {
        LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
        lqw.select(Knowledge::getId);
        List<Knowledge> knowledgeList = mindKnowledgeService.list(lqw);
        if(knowledgeList.isEmpty()){
            return Collections.emptyList();
        }
        return knowledgeList.stream().map(Knowledge::getId).collect(Collectors.toList());
    }

    @Override
    public List<Long> getAllDocumentIds() {
        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.select(Document::getId);
        List<Document> documentList = mindDocumentService.list(lqw);
        if(documentList.isEmpty()){
            return Collections.emptyList();
        }
        return documentList.stream().map(Document::getId).collect(Collectors.toList());
    }
}
