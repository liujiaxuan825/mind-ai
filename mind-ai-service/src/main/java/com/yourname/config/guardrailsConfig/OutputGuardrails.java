package com.yourname.config.guardrailsConfig;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.content.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutputGuardrails implements OutputGuardrail {

    private static final String HALLUCINATION_SYSTEM_PROMPT = """
            你是企业级事实校验官，任务：
            对比【AI回答】和【参考知识库】，判断是否存在幻觉。
            
            判定规则：
            1. 回答内容全部来自参考资料 → 无幻觉（false）
            2. 回答编造了资料中没有的制度、数据、流程 → 有幻觉（true）
            3. 回答模糊、猜测、不确定 → 有幻觉（true）
            
            输出：只输出 true / false
            """;

    private static final String REPROMPT_TEMPLATE = """
            你上一轮回答存在不准确/编造内容，请严格遵守：
            1. 只使用下面的【参考资料】回答
            2. 资料中没有 → 统一回复：抱歉，知识库暂无相关信息
            3. 禁止猜测、禁止编造、禁止扩展
            
            参考资料：
            %s
            """;

    private final ChatModel  chatModel;

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest params) {
        GuardrailRequestParams guardrailRequestParams = params.requestParams();
        String AiText = params.responseFromLLM().aiMessage().text();
        AugmentationResult augmentationResult = guardrailRequestParams.augmentationResult();
        String userQuestion = augmentationResult.chatMessage().toString();
        List<Content> contents = augmentationResult.contents();

        if(!AiText.contains("来源")){
            String newText = "请重新回答，并在回复的内容要标注来源哪一个文件";
            return reprompt("缺少来源", newText);
        }

        String judge = String.format("用户问题：%s\nAI回答：%s\\n参考资料：%s", userQuestion, AiText, contents);
        ChatResponse chatResponse = chatModel.chat(UserMessage.from(judge), SystemMessage.from(HALLUCINATION_SYSTEM_PROMPT));
        String isPass = chatResponse.aiMessage().text().trim().toLowerCase();
        boolean isSuccess = Boolean.parseBoolean(isPass);
        if(isSuccess){
            log.info("检测到幻觉，启用重试机制");
            return buildRepromptResult(contents);
        }
        log.info("未检出幻觉,通过");
        return success();
    }

    private OutputGuardrailResult buildRepromptResult(List<Content> contents) {
        String text = contents.stream().map(c -> c.textSegment().text().trim().toLowerCase()).collect(Collectors.joining("\n"));
        String result = String.format(REPROMPT_TEMPLATE, text);
        return this.reprompt("核实答案", result);
    }

}
