package com.liu.file.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.file.Service.IKnowledgeCacheService;
import com.liu.file.Service.IMindKnowledgeService;
import com.liu.file.domain.DTO.KnowledgeDTO;
import com.liu.file.domain.Entity.Knowledge;
import com.liu.file.domain.VO.KnowledgeVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-18
 */
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class MindKnowledgeController {
    
    private final IMindKnowledgeService mindKnowledgeService;
    
    private final IKnowledgeCacheService knowledgeCacheService;

    @PostMapping("/add")
    public Result<Void> createKnowledge(@RequestBody KnowledgeDTO knowledgeDTO){
        mindKnowledgeService.addKnowledge(knowledgeDTO);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageResultVO<KnowledgeVO>> pageKnowledge(@RequestBody PageRequestDTO pageDTO){
        return mindKnowledgeService.pageSelect(pageDTO);
    }

    @DeleteMapping("/kbId")
    public Result<Void> deleteKnowledge(@RequestBody List<Long> kbId){
        mindKnowledgeService.deleteKnowledge(kbId);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateKnowledge(@RequestBody KnowledgeDTO knowledgeDTO){
        mindKnowledgeService.updateKnowledge(knowledgeDTO);
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> countKnowledgeNum(){
        return mindKnowledgeService.countKnowledgeNum();
    }

    /**
     * 微服务暴露接口
     */
    @PostMapping("/list")
    public List<KnowledgeVO> list(@RequestBody List<Long> kbId){
        return knowledgeCacheService.getKnowledgeList(kbId);
    }

}
