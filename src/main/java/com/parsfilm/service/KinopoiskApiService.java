package com.parsfilm.service;

import com.parsfilm.dto.FilmApiDto;
import com.parsfilm.dto.FilmApiResponse;
import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.Film;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class KinopoiskApiService {

    private final WebClient webClient;
    @Value("${kinopoisk.api.token}")
    private String apiKey;

    @Value("${kinopoisk.api.base-url}")
    private String baseUrl;

    private RequestCounterService requestCounterService;
    private FilmSearchCriteriaService filmSearchCriteriaService;

    public KinopoiskApiService(WebClient webClient,
                               RequestCounterService requestCounterService,
                               FilmSearchCriteriaService filmSearchCriteriaService) {
        this.webClient = webClient;
        this.requestCounterService = requestCounterService;
        this.filmSearchCriteriaService = filmSearchCriteriaService;
    }

    //  какие критерии передаются - сформировать все возможные url для запросов
    public List<String> buildUrl(FilmSearchCriteria criteria) {
        List<String> allUrls = new ArrayList<>();

        // Если есть и страны, и жанры → вложенные циклы что бы собрать все возможные запросы.
        if (!criteria.getCountryIds().isEmpty() && !criteria.getGenreIds().isEmpty()) {
            for (Integer countryId : criteria.getCountryIds()) {
                for (Integer genreId : criteria.getGenreIds()) {
                    StringBuilder sb = new StringBuilder(baseUrl + "?");
                    sb.append("countries=").append(countryId).append("&");
                    sb.append("genres=").append(genreId).append("&");
                    appendCommonParams(sb, criteria);
                    allUrls.add(sb.toString());
                }
            }
        }
        // Только страны
        else if (!criteria.getCountryIds().isEmpty()) {
            for (Integer countryId : criteria.getCountryIds()) {
                StringBuilder sb = new StringBuilder(baseUrl + "?");
                sb.append("countries=").append(countryId).append("&");
                appendCommonParams(sb, criteria);
                allUrls.add(sb.toString());
            }
        }
        // Только жанры
        else if (!criteria.getGenreIds().isEmpty()) {
            for (Integer genreId : criteria.getGenreIds()) {
                StringBuilder sb = new StringBuilder(baseUrl + "?");
                sb.append("genres=").append(genreId).append("&");
                appendCommonParams(sb, criteria);
                allUrls.add(sb.toString());
            }
        }
        // Ни жанров, ни стран
        else {
            StringBuilder sb = new StringBuilder(baseUrl + "?");
            appendCommonParams(sb, criteria);
            allUrls.add(sb.toString());
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
    // Потому что за один запрос по фильтру приходит 20 фильмов, но потом каждый фильм это один запрос. А в апи лимиты
    public List<FilmDto> getFilmsByUrls(List<String> urls) {
        List<FilmDto> filmDtos = new ArrayList<>();

        // Пройтись по циклу, так как нужно пройтись по всем страницам, иначе приходят только 20 фильмов
        int filmsCollected = 0;
        int filmLimit = RequestCounterService.getDailyLimitRequests() * 20;
        for (String url : urls) {
            int page = 1;
            int totalPages = 1;

            // с начало сделать, и узнать сколько страниц будет
            do {
                try {
                    // если кол-во запрос не ноль или меньше или фильмов собрано не больше чем лимиты по запросам
                    if (requestCounterService.getRemainingRequests() <= 0 || filmsCollected >= filmLimit) {
                        return filmDtos;
                    }

                    // сбор первой страницы + 20 фильмов
                    String pageUrl = url + "&page=" + page;
                    FilmApiResponse filmApiResponse = webClient.get()
                            .uri(pageUrl)
                            .retrieve()
                            .bodyToMono(FilmApiResponse.class)
                            .block();

                    //счетчик что бы не превысить лимиты
                    requestCounterService.updateAndGetRemainingRequests();

                    // если что то получили то вытащить фильмы из страницы в общий список
                    if (filmApiResponse != null && filmApiResponse.getItems() != null) {
                        List<FilmDto> convertedFilms = filmApiResponse.getItems().stream()
                                .map(FilmApiDto::toFilmDto)
                                .toList();

                        filmDtos.addAll(convertedFilms);
                        filmsCollected += convertedFilms.size();
                        totalPages = filmApiResponse.getTotalPages();
                    }

                    page++;

                    // замедление запросов на 5 сек (лимиты на запросы по  фильтрам)
                    Thread.sleep(200);

                } catch (Exception ex) {
                    System.out.println("Ошибка при запросе " + url + ": " + ex.getMessage());
                    break;
                }

                // получаем страницы пока есть
            } while (page <= totalPages);
        }
        return filmDtos;
    }


    // Получить фильмы по id
    public List<FilmDto> getFilmsByIds(List<String> idUrls) {
        List<FilmDto> filmDtos = new ArrayList<>();
        // пройтись по циклу urlов и получить фильмы
        for (String url : idUrls) {
            FilmApiDto apiDto = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(FilmApiDto.class)
                    .block();
            //добавить фильмы в список
            if (apiDto != null) {
                filmDtos.add(apiDto.toFilmDto());
            }
        }
        return filmDtos;
    }


}
