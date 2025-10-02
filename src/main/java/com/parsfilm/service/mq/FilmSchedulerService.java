package com.parsfilm.service.mq;

import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.repository.GenreRep;
import com.parsfilm.service.FilmReportOrchestrationService;
import com.parsfilm.service.FilmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class FilmSchedulerService {

    private final FilmReportOrchestrationService orchestrationService;
    private final FilmService filmService;
    private final FilmMessageProducer producer;
    private final GenreRep genreRep;

    @Value("${app.scheduler.genres.monday.id}")
    private Integer mondayGenreId;

    @Value("${app.scheduler.genres.tuesday.id}")
    private Integer tuesdayGenreId;

    @Value("${app.scheduler.genres.wednesday.id}")
    private Integer wednesdayGenreId;

    @Value("${app.scheduler.genres.thursday.id}")
    private Integer thursdayGenreId;

    @Value("${app.scheduler.genres.friday.id}")
    private Integer fridayGenreId;

    @Value("${app.scheduler.genres.saturday.id}")
    private Integer saturdayGenreId;

    @Value("${app.scheduler.genres.sunday.id}")
    private Integer sundayGenreId;

    public FilmSchedulerService(FilmReportOrchestrationService orchestrationService,
                                FilmService filmService,
                                FilmMessageProducer producer,
                                GenreRep genreRep) {
        this.orchestrationService = orchestrationService;
        this.filmService = filmService;
        this.producer = producer;
        this.genreRep = genreRep;
    }

    // mvn spring-boot:run
    @Scheduled(cron = "0 40 15 * * ?")
    public void runScheduler() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        Integer genreId = getGenreIdForDay(today);

        if (genreId == null) {
            System.out.println(">>> Scheduler: для дня " + today + " ID жанра не задан");
            return;
        }

        System.out.println(">>> Scheduler: поиск по ID жанра: " + genreId);

        Genre genre = genreRep.findByIdApi(genreId).orElse(null);

        if (genre == null) {
            System.out.println(">>> Scheduler: жанр с ID " + genreId + " не найден в БД");
            return;
        }

        String genreName = genre.getName();
        System.out.println(">>> Scheduler: найден жанр '" + genreName + "'");

        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setGenres(List.of(genreName));
        criteria.setGenreIds(List.of(genreId));

        try {
            System.out.println(">>> Загрузка новых фильмов из API...");
            orchestrationService.generateReport(criteria);

            System.out.println(">>> Поиск фильмов в БД...");
            List<FilmDto> filmsFromDb = filmService.findAllByCriteria(criteria);

            if (filmsFromDb.size() > 50) {
                filmsFromDb = filmsFromDb.subList(0, 50);
            }

            producer.sendFilms(filmsFromDb);

            System.out.println(">>> Scheduler: найдено " + filmsFromDb.size() +
                               " фильмов по жанру '" + genreName + "' и отправлено в очередь");

        } catch (Exception e) {
            System.err.println(">>> Scheduler: ошибка при обработке ID " + genreId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Integer getGenreIdForDay(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> mondayGenreId;
            case TUESDAY -> tuesdayGenreId;
            case WEDNESDAY -> wednesdayGenreId;
            case THURSDAY -> thursdayGenreId;
            case FRIDAY -> fridayGenreId;
            case SATURDAY -> saturdayGenreId;
            case SUNDAY -> sundayGenreId;
        };
    }
}