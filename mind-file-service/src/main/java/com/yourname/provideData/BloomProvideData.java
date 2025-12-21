package com.yourname.provideData;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yourname.Service.IMindKnowledgeService;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.mind.infer.BloomDataProvider;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("bloomData")
public class BloomProvideData implements BloomDataProvider<Long> {

    @Resource
    private IMindKnowledgeService mindKnowledgeService;

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
}
