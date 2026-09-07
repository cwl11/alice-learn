package com.alice.learn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AppConfig {

    /**
     * DeepSeek V4 默认开启 thinking 模式：会先输出一段推理（按输出 token 计费，且流式时正文要等推理结束才开始）。
     * 范文/批改不需要深度推理，默认关闭以省钱、提速；如需开启把 AI_THINKING=true 即可。
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, @Value("${alice.ai.thinking:false}") boolean thinking) {
        OpenAiChatOptions defaults = OpenAiChatOptions.builder()
                .extraBody(Map.of("thinking", Map.of("type", thinking ? "enabled" : "disabled")))
                .build();
        return builder.defaultOptions(defaults).build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Alice Learn API")
                .version("0.1.0")
                .description("雅思学习网站接口文档"));
    }
}
