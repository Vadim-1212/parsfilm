package com.parsfilm.service;

import com.parsfilm.entity.helpEntity.RequestCounter;
import com.parsfilm.repository.RequestCounterRep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RequestCounterService {

    private RequestCounterRep repository;
    private static int DAILY_LIMIT_REQUESTS = 50; // дневной лимит 500, но он уходит за один запрос так как фильмов много, поэтому временно ставим поменьше фильмов.


    public RequestCounterService(RequestCounterRep repository) {
        this.repository = repository;
    }

    // Данный метод проверяет, обновляет раз в сутки, и считает кол-во запросов и
    // возращает кол-во оставшихся запросов, и прибавляет один запрос.
    // Крч что бы лимиты не привысить, и что бы все хватило для сбора данных по самим фильмам,
    // иначе просто названия коллекционирвоать смысла нет.
    @Transactional
    public int updateAndGetRemainingRequests() {
        LocalDate today = LocalDate.now();

        RequestCounter requestCounter = repository.findFirstByOrderByIdAsc()
                .orElseGet(() -> new RequestCounter(today, 0));

        // поставить сегодня и прибавить один запрос если в бд запись не сегодняшняя
        if (!requestCounter.getRequestDate().isEqual(today)) {
            requestCounter.setRequestDate(today);
            requestCounter.setRequests(1);
        } else {
            // иначе просто прибавить в бд + один запрос
            requestCounter.setRequests(requestCounter.getRequests() + 1);
        }
        repository.save(requestCounter);

        // Вывести на экран сколько осталось запросов
        int remaining = DAILY_LIMIT_REQUESTS - requestCounter.getRequests();
        System.out.println("Вот столько запросов осталось : " + remaining);
        return remaining;
    }

    // Возвращает текущее количество оставшихся запросов (НЕ увеличивает счётчик).
    public int getRemainingRequests() {
        LocalDate today = LocalDate.now();

        RequestCounter requestCounter = repository.findFirstByOrderByIdAsc()
                .orElseGet(() -> new RequestCounter(today, 0));

        // если день поменялся → значит лимит целиком доступен
        if (!requestCounter.getRequestDate().isEqual(today)) {
            return DAILY_LIMIT_REQUESTS;
        }

        // иначе возвращаем остаток
        return DAILY_LIMIT_REQUESTS - requestCounter.getRequests();
    }


    public static int getDailyLimitRequests() {
        return DAILY_LIMIT_REQUESTS;
    }

}
