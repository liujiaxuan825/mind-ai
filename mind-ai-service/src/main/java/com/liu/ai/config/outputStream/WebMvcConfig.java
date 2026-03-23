package com.liu.ai.config.outputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LangChain4jTokenStreamConverter tokenStreamConverter;

    public WebMvcConfig(LangChain4jTokenStreamConverter tokenStreamConverter) {
        this.tokenStreamConverter = tokenStreamConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, tokenStreamConverter);
    }
}