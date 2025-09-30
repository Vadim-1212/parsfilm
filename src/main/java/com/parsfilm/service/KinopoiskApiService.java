package com.parsfilm.service;

import com.parsfilm.dto.FilmApiDto;
import com.parsfilm.dto.FilmApiResponse;
import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class KinopoiskApiService {

    private final WebClient webClient;
    @Value("${kinopoisk.api.token}")
    private String apiKey;

    @Value("${kinopoisk.api.base-url}")
    private String baseUrl;

    private RequestCounterService requestCounterService;

    public KinopoiskApiService(WebClient webClient,
                               RequestCounterService requestCounterService) {
        this.webClient = webClient;
        this.requestCounterService = requestCounterService;
    }

    //  какие критерии передаются - сформировать все возможные url для запросов
    public List<String> buildUrl(FilmSearchCriteria criteria) {
        List<String> allUrls = new ArrayList<>();

        // ИСПРАВЛЕНО: используем Collections.singletonList вместо List.of
        List<Integer> countries = criteria.getCountryIds().isEmpty()
                ? Collections.singletonList(null)  // ← ВОТ ТУТ
                : criteria.getCountryIds();

        List<Integer> genres = criteria.getGenreIds().isEmpty()
                ? Collections.singletonList(null)  // ← И ТУТ
                : criteria.getGenreIds();

        // Декартово произведение
        for (Integer countryId : countries) {
            for (Integer genreId : genres) {
                StringBuilder sb = new StringBuilder(baseUrl + "?");

                if (countryId != null) {
                    sb.append("countries=").append(countryId).append("&");
                }
                if (genreId != null) {
                    sb.append("genres=").append(genreId).append("&");
                }

                appendCommonParams(sb, criteria);
                allUrls.add(sb.toString());
            }
        }

        for (String url : allUrls) {
            System.out.println("Вот такие url собраны для первичного поиска: " + url);
        }

        return allUrls;
    }

    // Вспомогательный метод — добавляет все остальные параметры к url-у
    public void appendCommonParams(StringBuilder sb, FilmSearchCriteria criteria) {
        if (criteria.getSortBy() != null) sb.append("order=").append(criteria.getSortBy()).append("&");
        if (criteria.getType() != null) sb.append("type=").append(criteria.getType().name()).append("&");
        if (criteria.getRatingFrom() != null) sb.append("ratingFrom=").append(criteria.getRatingFrom()).append("&");
        if (criteria.getRatingTo() != null) sb.append("ratingTo=").append(criteria.getRatingTo()).append("&");
        if (criteria.getYearFrom() != null) sb.append("yearFrom=").append(criteria.getYearFrom()).append("&");
        if (criteria.getYearTo() != null) sb.append("yearTo=").append(criteria.getYearTo()).append("&");
        if (criteria.getKeyword() != null) sb.append("keyword=").append(criteria.getKeyword()).append("&");
    }

    // Получить все юрл из первого запроса, что бы заного пойти в апи за фильмами.
    // Так как с первого запроса приходят не все данные
    public List<String> buildUrlForIds(List<FilmDto> films) {
        List<String> allUrls = films.stream()
                .filter(f -> f.getKinopoiskId() != null) // пропускаем null
                .map(f -> baseUrl + "/" + f.getKinopoiskId())
                .toList();

        for (String url : allUrls) {
            System.out.println("Вот такие url собраны для вторичного поиска: " + url);
        }

        return allUrls;
    }

    // Получить фильмы по url-ам и не привысить кол-во фильмов в половину кол-ва запросов.
    public List<FilmDto> getFilmsByUrls(List<String> urls) {
        List<FilmDto> filmDtos = new ArrayList<>();
        int filmsCollected = 0;

        // Простая формула: половина запросов на сбор, половина на детали
        int remainingRequests = requestCounterService.getRemainingRequests();
        int requestsForFiltering = Math.max(1, remainingRequests / 2);
        int maxFilms = requestsForFiltering * 20;

        System.out.println(">>> Доступно запросов: " + remainingRequests);
        System.out.println(">>> Используем для фильтрации: " + requestsForFiltering);
        System.out.println(">>> Макс. фильмов для сбора: " + maxFilms);

        for (String url : urls) {
            int page = 1;
            int totalPages = 1;

            do {
                try {
                    if (filmsCollected >= maxFilms) {
                        System.out.println(">>> СТОП: Собрано " + filmsCollected + " фильмов, лимит " + maxFilms);
                        return filmDtos;
                    }

                    String pageUrl = url + "&page=" + page;
                    System.out.println(">>> Отправляю запрос: " + pageUrl);

                    FilmApiResponse filmApiResponse = webClient.get()
                            .uri(pageUrl)
                            .retrieve()
                            .bodyToMono(FilmApiResponse.class)
                            .block();

                    System.out.println(">>> Получен ответ: " + (filmApiResponse == null ? "NULL" : "OK, items=" + (filmApiResponse.getItems() == null ? "null" : filmApiResponse.getItems().size())));

                    if (filmApiResponse != null && filmApiResponse.getItems() != null) {
                        List<FilmDto> convertedFilms = filmApiResponse.getItems().stream()
                                .map(FilmApiDto::toFilmDto)
                                .toList();

                        filmDtos.addAll(convertedFilms);
                        filmsCollected += convertedFilms.size();
                        totalPages = filmApiResponse.getTotalPages();
                    }

                    page++;
                    Thread.sleep(200);

                } catch (Exception ex) {
                    System.out.println("Ошибка при запросе " + url + ": " + ex.getMessage());
                    break;
                }

            } while (page <= totalPages);
        }
        return filmDtos;
    }
    // Получить фильмы по id
    public List<FilmDto> getFilmsByIds(List<String> idUrls) {
        List<FilmDto> filmDtos = new ArrayList<>();

        for (String url : idUrls) {
            try {
                log.info("Отправляю запрос в API {}", url);

                FilmApiDto apiDto = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(FilmApiDto.class)
                        .block();

                log.debug("Ответ от API {}: {}", url, apiDto);

                if (apiDto != null) {
                    filmDtos.add(apiDto.toFilmDto());
                } else {
                    // Запрос был заблокирован interceptor'ом
                    log.warn("Запрос {} вернул null - возможно лимит исчерпан", url);
                }

            } catch (Exception ex) {
                log.error("Ошибка при запросе {}: {}", url, ex.getMessage());
                // Не прерываем цикл - продолжаем с остальными
            }
        }

        System.out.println(">>> getFilmsByIds: собрано " + filmDtos.size() + " из " + idUrls.size() + " запросов");
        return filmDtos;
    }

}
