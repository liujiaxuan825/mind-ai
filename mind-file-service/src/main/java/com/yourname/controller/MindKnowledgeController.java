package com.yourname.controller;


import com.yourname.Service.IMindKnowledgeService;
import com.yourname.domain.DTO.KnowledgeDTO;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
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

    @PostMapping("/add")
    public Result<Void> createKnowledge(@RequestBody KnowledgeDTO knowledgeDTO){
        mindKnowledgeService.addKnowledge(knowledgeDTO);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageResultVO<KnowledgeVO>> pageKnowledge(@RequestBody KnowledgeDTO knowledgeDTO,
                                                           PageRequestDTO pageDTO){
        return mindKnowledgeService.pageSelect(knowledgeDTO,pageDTO);
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

}
