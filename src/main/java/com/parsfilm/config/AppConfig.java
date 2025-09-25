package com.parsfilm.config;

import io.netty.channel.ChannelOption;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${kinopoisk.api.base-url}")
    private String baseUrl;

    @Value("${kinopoisk.api.token}")
    private String token;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-API-KEY", token)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .resolver(DefaultAddressResolverGroup.INSTANCE)
                                .responseTimeout(Duration.ofSeconds(60))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                ))
                .filter((request, next) -> next.exchange(request)
                        .onErrorResume(WebClientResponseException.class, ex -> {
                            // Логируем статус + тело ошибки
                            System.err.println("WebClient error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                            return Mono.empty(); // не кидаем исключение дальше
                        })
                        .onErrorResume(Exception.class, ex -> {
                            // Логируем прочие ошибки (например timeout)
                            System.err.println("Unexpected error in WebClient: " + ex.getMessage());
                            return Mono.empty();
                        })
                )
                .build();
    }


}
