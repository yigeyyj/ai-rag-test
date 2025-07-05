package com.hanlinyang.trigger.controller;

import com.hanlinyang.api.IAiService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ollama/")
public class OllamaController implements IAiService {

    @Resource
    private OllamaChatClient chatClient;

    @Override
    @GetMapping("/generate")
    public ChatResponse generate(String model, String message) {
        return chatClient.call(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    @Override
    @GetMapping("/generate_stream")
    public Flux<ChatResponse> generateStream(String model, String message) {
        return chatClient.stream(new Prompt(
                message,
                OllamaOptions.create()
                        .withModel(model)
        ));
    }
}
