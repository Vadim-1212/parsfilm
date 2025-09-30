package com.parsfilm.service;

import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmMapper;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.Film;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class FilmReportOrchestrationService {

    private final FilmService filmService;
    private final KinopoiskApiService kinopoiskApiService;
    private final FilmReportService filmReportService;
    private final FilmSearchCriteriaService criteriaService;
    private final FilmMapper filmMapper;

    public FilmReportOrchestrationService(FilmService filmService,
                                          KinopoiskApiService kinopoiskApiService,
                                          FilmReportService filmReportService,
                                          FilmSearchCriteriaService criteriaService,
                                          FilmMapper filmMapper) {
        this.filmService = filmService;
        this.kinopoiskApiService = kinopoiskApiService;
        this.filmReportService = filmReportService;
        this.criteriaService = criteriaService;
        this.filmMapper = filmMapper;
    }

    public File generateReport(FilmSearchCriteria criteria) {
        FilmSearchCriteria normalizedCriteria = criteriaService.normalizeCriteria(criteria);

        List<FilmDto> filmsFromApi = fetchFilmsFromApi(normalizedCriteria);
        System.out.println(">>> Получено из API: " + filmsFromApi.size());

        saveFilmsToDatabase(filmsFromApi);

        List<FilmDto> filmsFromDatabase = filmService.findAllByCriteria(normalizedCriteria);
        System.out.println(">>> Получено из БД по критериям: " + filmsFromDatabase.size());

        File report = filmReportService.generateReportFiles(filmsFromDatabase);
        System.out.println(">>> Отчёт создан: " + (report != null));

        return report;
    }

    private List<FilmDto> fetchFilmsFromApi(FilmSearchCriteria criteria) {
        List<String> urls = kinopoiskApiService.buildUrl(criteria);
        System.out.println("1. URLs построены: " + urls.size());

        List<FilmDto> filmDtos = kinopoiskApiService.getFilmsByUrls(urls);
        System.out.println("2. Краткие данные получены: " + filmDtos.size());

        List<String> detailUrls = kinopoiskApiService.buildUrlForIds(filmDtos);
        System.out.println("3. URLs для деталей построены: " + detailUrls.size());

        List<FilmDto> filmDetails = kinopoiskApiService.getFilmsByIds(detailUrls);
        System.out.println("4. Детали получены: " + filmDetails.size());

        List<FilmDto> result = deduplicateByKinopoiskId(filmDetails);
        System.out.println("5. После дедупликации: " + result.size());

        return result;
    }

    private void saveFilmsToDatabase(List<FilmDto> filmDtos) {
        System.out.println(">>> Сохраняем в БД: " + filmDtos.size() + " фильмов");

        List<Film> films = filmDtos.stream()
                .map(filmMapper::toFilm)
                .toList();

        List<Film> saved = filmService.saveAll(films);
        System.out.println(">>> Сохранено в БД: " + saved.size() + " фильмов");
    }

    private List<FilmDto> deduplicateByKinopoiskId(List<FilmDto> filmDtos) {
        if (filmDtos == null || filmDtos.isEmpty()) {
            return List.of();
        }

        Map<Long, FilmDto> uniqueFilms = new LinkedHashMap<>();
        for (FilmDto dto : filmDtos) {
            Long kinopoiskId = dto.getKinopoiskId();
            uniqueFilms.putIfAbsent(kinopoiskId, dto);
        }

        return new ArrayList<>(uniqueFilms.values());
    }
}