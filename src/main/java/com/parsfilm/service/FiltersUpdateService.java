package com.parsfilm.service;// package com.parsfilm.service;

import com.parsfilm.dto.FiltersResponse;
import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.GenreRep;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
//Этот класс FiltersSyncService при старте приложения и по вызову метода syncAll() загружает из внешнего API
// список жанров и стран и сохраняет их в базу, если их там ещё нет.

@Service
public class FiltersUpdateService {


    private final WebClient webClient;
    private final GenreRep genreRep;
    private final CountryRep countryRep;


    public FiltersUpdateService(WebClient webClient, GenreRep genreRep, CountryRep countryRep) {
        this.webClient = webClient;
        this.genreRep = genreRep;
        this.countryRep = countryRep;
    }

    // загружаем заранее в бд все жанры и страны
    @Bean
    ApplicationRunner syncFiltersOnStartup(FiltersUpdateService filtersUpdateService) {
        return args -> filtersUpdateService.syncAll();
    }

    @Transactional
    public void syncAll() {
        try {
            //
            FiltersResponse resp = webClient.get().uri("/filters")
                    .retrieve()
                    .bodyToMono(FiltersResponse.class)
                    .doOnError(ex -> {
                        System.err.println("Ошибка при запросе фильтров: " + ex.getMessage());
                    })
                    .onErrorResume(ex -> Mono.empty())
                    .block();

            if (resp == null) {
                System.out.println("Ответ от API пустой, syncAll пропущен");
                return;
            }

            // создадим новые жанры и страны если их нет в бд
            if (resp.getGenres() != null) {
                resp.getGenres().forEach(g -> {
                    try {
                        Genre e = genreRep.findByIdApi(g.getId()).orElse(new Genre());
                        e.setIdApi(g.getId());
                        e.setName(g.getGenre());
                        genreRep.save(e);
                    } catch (Exception ex) {
                        System.err.println("Ошибка при сохранении жанра " + g.getGenre() + ": " + ex.getMessage());
                    }
                });
            }

            if (resp.getCountries() != null) {
                resp.getCountries().forEach(c -> {
                    try {
                        Country e = countryRep.findByIdApi(c.getId()).orElse(new Country());
                        e.setIdApi(c.getId());
                        e.setName(c.getCountry());
                        countryRep.save(e);
                    } catch (Exception ex) {
                        System.err.println("Ошибка при сохранении страны " + c.getCountry() + ": " + ex.getMessage());
                    }
                });
            }

            System.out.println("syncAll завершён успешно");

        } catch (Exception e) {
            System.err.println("Глобальная ошибка syncAll: " + e.getMessage());
        }
    }




}
