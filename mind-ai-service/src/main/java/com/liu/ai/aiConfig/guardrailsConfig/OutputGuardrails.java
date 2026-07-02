package com.liu.ai.aiConfig.guardrailsConfig;

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
import org.springframework.util.StringUtils;
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
            1. 回答内容是基于参考资料 → 无幻觉（false）
            2. 回答编造了资料中没有的制度、数据、流程 → 有幻觉（true）
            3. 回答模糊、猜测、不确定 → 有幻觉（true）

            输出：只输出 true / false，不要加任何其他文字
            """;

    private static final String REPROMPT_TEMPLATE = """
            你上一轮回答存在不准确/编造内容，请严格遵守：
            1. 只使用下面的【参考资料】回答
            2. 资料中没有 → 统一回复：抱歉，知识库暂无相关信息
            3. 禁止猜测、禁止编造

            参考资料：
            %s
            """;

    private final ChatModel chatModel;

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest params) {
        try {
            String aiAnswer = params.responseFromLLM().aiMessage().text();
            if (aiAnswer.contains("我叫璐璐") || aiAnswer.contains("官人哨子")) {
                log.info("[输出护栏] 身份回答，豁免校验，直接通过");
                return OutputGuardrailResult.success();
            }
            GuardrailRequestParams requestParams = params.requestParams();
            AugmentationResult augmentationResult = requestParams.augmentationResult();

            if (augmentationResult == null || augmentationResult.contents() == null || augmentationResult.contents().isEmpty()) {
                log.info("[输出护栏] 无参考资料，普通对话，放行");
                return success();
            }

            List<Content> contents = augmentationResult.contents();
            if (!aiAnswer.contains("来源") && !aiAnswer.contains("抱歉，知识库暂无相关信息")) {
                log.info("[输出护栏] 回答缺少来源，要求重新生成");
                return reprompt("缺少来源", "请重新回答，并在回复中标注来源");
            }


            String referenceContent = contents.stream()
                    .map(Content::textSegment)
                    .map(segment -> segment.text().trim()) // 修复 7：规范获取文本
                    .collect(Collectors.joining("\n\n"));

            String userPrompt = String.format("""
                    AI回答：%s
                    参考资料：%s
                    """, aiAnswer, referenceContent);

            ChatResponse chatResponse = chatModel.chat(
                    SystemMessage.from(HALLUCINATION_SYSTEM_PROMPT),
                    UserMessage.from(userPrompt)
            );

            String llmResult = chatResponse.aiMessage().text().trim();
            log.info("[输出护栏] 幻觉校验原始结果：{}", llmResult);

            boolean hasHallucination = parseHallucinationResult(llmResult);

            if (hasHallucination) {
                log.warn("[输出护栏] 检测到幻觉，启用重试");
                return buildRepromptResult(contents);
            }

            log.info("[输出护栏] 未检测到幻觉，通过");
            return success();

        } catch (Exception e) {
            log.error("[输出护栏] 校验异常，容错放行", e);
            return success();
        }
    }


    private boolean parseHallucinationResult(String result) {
        if (!StringUtils.hasText(result)) return false;

        String lower = result.toLowerCase();
        return lower.contains("true") || lower.contains("有幻觉") || lower.contains("存在");
    }

    private OutputGuardrailResult buildRepromptResult(List<Content> contents) {
        String text = contents.stream()
                .map(c -> c.textSegment().text().trim())
                .collect(Collectors.joining("\n\n"));
        String repromptContent = String.format(REPROMPT_TEMPLATE, text);
        return reprompt("检测到幻觉，重新生成", repromptContent);
    }
}