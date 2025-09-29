package com.parsfilm.service;

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
}