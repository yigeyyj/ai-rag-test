package com.hanlinyang.api;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

public interface IAiService {

    ChatResponse generate(String model, String message);



    Flux<ChatResponse> generateStream(String model, String message);


    Flux<ChatResponse> generateStreamRag( String model, String ragTag, String message);

    }
