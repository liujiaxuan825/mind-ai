package com.liu.ai.config.outputStream;

import dev.langchain4j.service.TokenStream;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

@Component
public class LangChain4jTokenStreamConverter extends AbstractHttpMessageConverter<TokenStream> {

    public LangChain4jTokenStreamConverter() {
        super(MediaType.TEXT_EVENT_STREAM); // 流式响应媒体类型
    }
    @Override
    protected boolean supports(Class<?> clazz) {
        return TokenStream.class.isAssignableFrom(clazz);
    }

    @Override
    protected TokenStream readInternal(Class<? extends TokenStream> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(TokenStream tokenStream, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        OutputStream os = outputMessage.getBody();
        CountDownLatch latch = new CountDownLatch(1);

        // 关键修复：严格按顺序配置 TokenStream，确保 onError/ignoreErrors 只调用一次
        try {
            // 1. 先配置错误处理（二选一，且仅调用一次）
            tokenStream.onError(error -> {
                try {
                    os.write(("data: 错误：" + error.getMessage() + "\n\n").getBytes(StandardCharsets.UTF_8));
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });

            // 2. 配置部分响应（逐字生成核心）
            tokenStream.onPartialResponse(partialResponse -> {
                try {
                    String sseLine = "data: " + partialResponse + "\n\n";
                    os.write(sseLine.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                } catch (IOException e) {
                    throw new RuntimeException("流式输出失败", e);
                }
            });

            // 3. 配置完整响应（结束标记）
            tokenStream.onCompleteResponse(completeResponse -> {
                try {
                    os.write("data: [DONE]\n\n".getBytes(StandardCharsets.UTF_8));
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });

            // 4. 启动流式输出（必须在所有配置完成后调用）
            tokenStream.start();

            // 5. 等待流式结束
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("流式响应被中断", e);
        } finally {
            os.close();
        }
    }
}
