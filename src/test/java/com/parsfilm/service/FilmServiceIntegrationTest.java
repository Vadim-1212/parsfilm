package com.parsfilm.service;

import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.Film;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.FilmRep;
import com.parsfilm.repository.GenreRep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FilmServiceIntegrationTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private FilmRep filmRep;

    @Autowired
    private GenreRep genreRep;

    @Autowired
    private CountryRep countryRep;

    @BeforeEach
    void cleanDatabase() {
        filmRep.deleteAll();
        genreRep.deleteAll();
        countryRep.deleteAll();
    }

    @Test
    void shouldSaveNewFilmWithNewGenresAndCountries() {
        // Arrange
        Film film = TestDataBuilder.createFilmWithGenresAndCountries(
                12345L,
                "Тестовый фильм",
                Set.of("Комедия", "Драма"),
                Set.of("Россия", "США")
        );

        // Act
        List<Film> savedFilms = filmService.saveAll(List.of(film));

        // Assert
        assertEquals(1, savedFilms.size());

        Film savedFilm = savedFilms.get(0);
        assertEquals("Тестовый фильм", savedFilm.getNameRu());
        assertEquals(12345L, savedFilm.getKinopoiskId());

        // Проверяем что жанры и страны сохранились
        assertEquals(2, savedFilm.getGenres().size());
        assertEquals(2, savedFilm.getCountries().size());

        // Проверяем что в БД создались записи
        assertEquals(2, genreRep.count());
        assertEquals(2, countryRep.count());
        assertEquals(1, filmRep.count());
    }

    @Test
    void shouldNotCreateDuplicateGenresAndCountries() {
        // Arrange - создаём 2 фильма с одинаковыми жанрами
        Film film1 = TestDataBuilder.createFilmWithGenresAndCountries(
                11111L,
                "Фильм 1",
                Set.of("Комедия"),
                Set.of("Россия")
        );

        Film film2 = TestDataBuilder.createFilmWithGenresAndCountries(
                22222L,
                "Фильм 2",
                Set.of("Комедия"), // Тот же жанр!
                Set.of("Россия")   // Та же страна!
        );

        // Act
        filmService.saveAll(List.of(film1, film2));

        // Assert - должен быть только 1 жанр и 1 страна
        assertEquals(1, genreRep.count(), "Жанр 'Комедия' должен быть только один!");
        assertEquals(1, countryRep.count(), "Страна 'Россия' должна быть только одна!");
        assertEquals(2, filmRep.count(), "Но фильмов должно быть 2!");
    }

    @Test
    void shouldFindFilmsByYearRange() {
        // Arrange - создаём фильмы разных годов
        Film film2010 = TestDataBuilder.createFilm(1L, "Начало");
        film2010.setYear(2010);

        Film film2014 = TestDataBuilder.createFilm(2L, "Интерстеллар");
        film2014.setYear(2014);

        Film film1973 = TestDataBuilder.createFilm(3L, "Иван Васильевич");
        film1973.setYear(1973);

        filmService.saveAll(List.of(film2010, film2014, film1973));

        // Act - ищем фильмы с 2000 по 2015 год
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setYearFrom(2000);
        criteria.setYearTo(2015);

        List<FilmDto> result = filmService.findAllByCriteria(criteria);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> f.getNameRu().equals("Начало")));
        assertTrue(result.stream().anyMatch(f -> f.getNameRu().equals("Интерстеллар")));
    }

    @Test
    void shouldFindFilmsByRatingRange() {
        // Arrange
        Film highRated = TestDataBuilder.createFilm(1L, "Высокий рейтинг");
        highRated.setRatingKinopoisk(9.0);

        Film midRated = TestDataBuilder.createFilm(2L, "Средний рейтинг");
        midRated.setRatingKinopoisk(7.0);

        Film lowRated = TestDataBuilder.createFilm(3L, "Низкий рейтинг");
        lowRated.setRatingKinopoisk(5.0);

        filmService.saveAll(List.of(highRated, midRated, lowRated));

        // Act - ищем фильмы с рейтингом от 8.0
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setRatingFrom(8.0);
        criteria.setRatingTo(10.0);

        List<FilmDto> result = filmService.findAllByCriteria(criteria);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Высокий рейтинг", result.get(0).getNameRu());
    }

    @Test
    void shouldFindFilmsByKeyword() {
        // Arrange
        Film film1 = TestDataBuilder.createFilm(1L, "Матрица");
        film1.setDescription("Фильм про виртуальную реальность");

        Film film2 = TestDataBuilder.createFilm(2L, "Начало");
        film2.setDescription("Фильм про сны");

        Film film3 = TestDataBuilder.createFilm(3L, "Интерстеллар");
        film3.setDescription("Космические путешествия");

        filmService.saveAll(List.of(film1, film2, film3));

        // Act - ищем по ключевому слову "реальность"
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setKeyword("реальность");

        List<FilmDto> result = filmService.findAllByCriteria(criteria);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Матрица", result.get(0).getNameRu());
    }

    @Test
    void shouldFindFilmsByGenre() {
        // Arrange
        Film sciFi1 = TestDataBuilder.createFilmWithGenresAndCountries(
                1L, "Интерстеллар",
                Set.of("Фантастика"),
                Set.of("США")
        );

        Film sciFi2 = TestDataBuilder.createFilmWithGenresAndCountries(
                2L, "Матрица",
                Set.of("Фантастика"),
                Set.of("США")
        );

        Film comedy = TestDataBuilder.createFilmWithGenresAndCountries(
                3L, "Иван Васильевич",
                Set.of("Комедия"),
                Set.of("СССР")
        );

        filmService.saveAll(List.of(sciFi1, sciFi2, comedy));

        // Act - ищем фантастику
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setGenres(List.of("Фантастика"));

        List<FilmDto> result = filmService.findAllByCriteria(criteria);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f ->
                f.getGenres().stream().anyMatch(g -> g.equals("Фантастика"))
        ));
    }

    @Test
    void shouldFindFilmsByCountry() {
        // Arrange
        Film usa1 = TestDataBuilder.createFilmWithGenresAndCountries(
                1L, "Матрица",
                Set.of("Фантастика"),
                Set.of("США")
        );

        Film russia = TestDataBuilder.createFilmWithGenresAndCountries(
                2L, "Брат",
                Set.of("Драма"),
                Set.of("Россия")
        );

        filmService.saveAll(List.of(usa1, russia));

        // Act
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setCountries(List.of("США"));

        List<FilmDto> result = filmService.findAllByCriteria(criteria);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Матрица", result.get(0).getNameRu());
    }


}