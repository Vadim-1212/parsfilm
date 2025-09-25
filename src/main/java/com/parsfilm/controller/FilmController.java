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
    private final FiltersSyncService filtersSyncService;
    private final FilmMapper filmMapper;

    public FilmController(FilmService filmService,
                          KinopoiskApiService kinopoiskApiService,
                          EmailService emailService,
                          FilmSearchCriteriaService criteriaService,
                          FiltersSyncService filtersSyncService,
                          FilmMapper filmMapper) {
        this.filmService = filmService;
        this.kinopoiskApiService = kinopoiskApiService;
        this.emailService = emailService;
        this.criteriaService = criteriaService;
        this.filtersSyncService = filtersSyncService;
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
            filtersSyncService.syncAll();

            List<String> urls = kinopoiskApiService.buildUrl(normalizedCriteria);
            List<FilmDto> filmDtos = kinopoiskApiService.getFilmsByUrls(urls);

            List<FilmDto> filmDetailsDtos = kinopoiskApiService.getFilmsByIds(
                    kinopoiskApiService.buildUrlForIds(filmDtos)
            );

            List<FilmDto> uniqueFilmDetails = deduplicateByKinopoiskId(filmDetailsDtos);

            List<Film> films = uniqueFilmDetails.stream()
                    .map(filmMapper::toFilm)
                    .toList();

            filmService.saveAll(films);

            List<FilmDto> filmsFromDatabase = filmService.findAllByCriteria(normalizedCriteria);

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

    private List<FilmDto> combineUniqueByKinopoiskId(List<FilmDto>... filmSources) {
        Map<Long, FilmDto> uniqueFilms = new LinkedHashMap<>();

        if (filmSources != null) {
            for (List<FilmDto> source : filmSources) {
                if (source == null) {
                    continue;
                }
                for (FilmDto dto : source) {
                    if (dto == null) {
                        continue;
                    }
                    uniqueFilms.putIfAbsent(dto.getKinopoiskId(), dto);
                }
            }
        }

        return new ArrayList<>(uniqueFilms.values());
    }
}


