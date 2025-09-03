package com.example.chatService.repository.httpClient;

import com.example.chatService.dto.request.TranslateRequest;
import com.example.dto.TranslateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "TranslateClient",url = "${app.translate.url}")
public interface translateClient {
    @PostMapping
    TranslateResponse translate(@RequestBody TranslateRequest content);

}
