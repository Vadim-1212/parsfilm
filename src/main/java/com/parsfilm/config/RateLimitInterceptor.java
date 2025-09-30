package com.parsfilm.config;

import com.parsfilm.service.RequestCounterService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class RateLimitInterceptor implements ExchangeFilterFunction {

    private final RequestCounterService requestCounterService;

    public RateLimitInterceptor(RequestCounterService requestCounterService) {
        this.requestCounterService = requestCounterService;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // Проверяем лимит ПЕРЕД запросом
        int remaining = requestCounterService.getRemainingRequests();

        if (remaining <= 0) {
            System.out.println(">>> БЛОКИРОВКА: Лимит исчерпан, запрос не отправлен: " + request.url());
            // Возвращаем пустой результат вместо ошибки
            return Mono.empty();
        }

        System.out.println(">>> Отправка через interceptor: " + request.url());

        // СНАЧАЛА увеличиваем счётчик, ПОТОМ отправляем запрос
        requestCounterService.updateAndGetRemainingRequests();

        // Отправляем запрос
        return next.exchange(request)
                .doOnSuccess(response -> {
                    int updated = requestCounterService.getRemainingRequests();
                    System.out.println(">>> Запрос выполнен. Осталось: " + updated);
                });
    }}