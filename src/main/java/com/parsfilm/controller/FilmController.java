package com.parsfilm.controller;

import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmMapper;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.Film;
import com.parsfilm.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/films")
public class FilmController {

    private final FilmService filmService;
    private final KinopoiskApiService kinopoiskApiService;
    private final EmailService emailService;
    private final FilmSearchCriteriaService criteriaService;
    private final FilmMapper filmMapper;

    public FilmController(FilmService filmService,
                          KinopoiskApiService kinopoiskApiService,
                          EmailService emailService,
                          FilmSearchCriteriaService criteriaService,
                          FilmMapper filmMapper,
                          FiltersUpdateService filtersUpdateService
                          ) {
        this.filmService = filmService;
        this.kinopoiskApiService = kinopoiskApiService;
        this.emailService = emailService;
        this.criteriaService = criteriaService;
        this.filmMapper = filmMapper;
    }

    @PostMapping("/report-email")
    public ResponseEntity<String> generateAndSendReport(@RequestBody FilmSearchCriteria criteria) {
        String email = criteria.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("email обязателен");
        }

        File zipFile = null;
        try {

            FilmSearchCriteria normalizedCriteria = criteriaService.normalizeCriteria(criteria);

            // получить список все возможных urls
            List<String> urls = kinopoiskApiService.buildUrl(normalizedCriteria);

            // получить список фильмов по фильтру
            List<FilmDto> filmDtos = kinopoiskApiService.getFilmsByUrls(urls);

            // получить список всех деталей для фильмов по id
            List<FilmDto> filmDetailsDtos = kinopoiskApiService.getFilmsByIds(

                    // получить url для запросов по id и вставить в параметры метода
                    kinopoiskApiService.buildUrlForIds(filmDtos)
            );

            // убираем дубликаты фильмов если они попались в запросе.
            List<FilmDto> uniqueFilmDetails = deduplicateByKinopoiskId(filmDetailsDtos);

            // из дто делаем сущность фильм
            List<Film> films = uniqueFilmDetails.stream()
                    .map(filmMapper::toFilm)
                    .toList();

            // сохраняем все фильмы
            filmService.saveAll(films);

            //собираем все из бд по фильтру
            List<FilmDto> filmsFromDatabase = filmService.findAllByCriteria(normalizedCriteria);

            //создаем файл для отправки
            zipFile = filmService.generateReportFiles(filmsFromDatabase);
            if (zipFile == null) {
                return ResponseEntity.internalServerError().body("Ошибка при создании отчёта");
            }

            emailService.sendReportByEmail(email, zipFile);

            return ResponseEntity.ok("Отчёт успешно отправлен на " + email);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        } finally {
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
        }
    }

    // в итоге получаем лист фильмов
    private List<FilmDto> deduplicateByKinopoiskId(List<FilmDto> filmDtos) {

        // пустой список если ничего нет
        if (filmDtos == null || filmDtos.isEmpty()) {
            return List.of();
        }

        // сохраняя порядок вставки (не вспомнил нахрена, но и менять боюсь ))) )
        Map<Long, FilmDto> uniqueFilms = new LinkedHashMap<>();
        for (FilmDto dto : filmDtos) {
            Long kinopoiskId = dto.getKinopoiskId();
            uniqueFilms.putIfAbsent(kinopoiskId, dto);
        }

        return new ArrayList<>(uniqueFilms.values());
    }

}


