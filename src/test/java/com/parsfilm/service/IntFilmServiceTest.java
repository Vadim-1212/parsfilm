package com.parsfilm.service;

import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmMapper;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.Film;
import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.FilmRep;
import com.parsfilm.repository.GenreRep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegrFilmServiceTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private FilmRep filmRep;

    @Autowired
    private GenreRep genreRep;

    @Autowired
    private CountryRep countryRep;

    @Autowired
    private FilmMapper filmMapper;

    @BeforeEach
    void cleanDatabase() {
        // Очищаем БД перед каждым тестом
        filmRep.deleteAll();
        genreRep.deleteAll();
        countryRep.deleteAll();
    }

    // ============================================
    // ТЕСТ 1: Сохранение одного фильма
    // ============================================
    @Test
    void shouldSaveOneFilm() {
        // ШАГ 1: Создаём фильм
        Film film = new Film();
        film.setKinopoiskId(12345L);
        film.setNameRu("Иван Васильевич меняет профессию");
        film.setNameEn("Ivan Vasilyevich Changes Profession");
        film.setYear(1973);
        film.setRatingKinopoisk(8.8);
        film.setFilmTypes(FilmType.FILM);
        film.setDescription("Легендарная советская комедия");

        // ШАГ 2: Сохраняем в БД
        List<Film> saved = filmService.saveAll(List.of(film));

        // ШАГ 3: Проверяем что сохранился
        assertEquals(1, saved.size(), "Должен сохраниться 1 фильм");
        assertNotNull(saved.get(0).getId(), "У фильма должен появиться ID");

        // ШАГ 4: Проверяем что можем прочитать из БД
        List<Film> allFilms = filmRep.findAll();
        assertEquals(1, allFilms.size(), "В БД должен быть 1 фильм");
        assertEquals("Иван Васильевич меняет профессию", allFilms.get(0).getNameRu());

        System.out.println("✅ Тест пройден: фильм сохранён");
    }

    // ============================================
    // ТЕСТ 2: Сохранение фильма с жанрами и странами
    // ============================================
    @Test
    void shouldSaveFilmWithGenresAndCountries() {
        // ШАГ 1: Создаём жанр
        Genre comedy = new Genre();
        comedy.setName("комедия");
        comedy.setIdApi(1);

        // ШАГ 2: Создаём страну
        Country ussr = new Country();
        ussr.setName("СССР");
        ussr.setIdApi(1);

        // ШАГ 3: Создаём фильм и связываем с жанром и страной
        Film film = new Film();
        film.setKinopoiskId(12345L);
        film.setNameRu("Операция Ы");
        film.setYear(1965);
        film.setFilmTypes(FilmType.FILM);

        Set<Genre> genres = new HashSet<>();
        genres.add(comedy);
        film.setGenres(genres);

        Set<Country> countries = new HashSet<>();
        countries.add(ussr);
        film.setCountries(countries);

        // ШАГ 4: Сохраняем
        List<Film> saved = filmService.saveAll(List.of(film));

        // ШАГ 5: Проверяем связи
        Film savedFilm = saved.get(0);
        assertEquals(1, savedFilm.getGenres().size(), "Должен быть 1 жанр");
        assertEquals(1, savedFilm.getCountries().size(), "Должна быть 1 страна");
        assertEquals("комедия", savedFilm.getGenres().iterator().next().getName());
        assertEquals("СССР", savedFilm.getCountries().iterator().next().getName());

        // ШАГ 6: Проверяем что жанр и страна созданы в БД
        assertEquals(1, genreRep.count(), "Должен быть 1 жанр в БД");
        assertEquals(1, countryRep.count(), "Должна быть 1 страна в БД");

        System.out.println("✅ Тест пройден: связи ManyToMany работают");
    }

    // ============================================
    // ТЕСТ 3: Поиск по году
    // ============================================
    @Test
    void shouldFindFilmsByYear() {
        // ШАГ 1: Создаём 3 фильма разных годов
        createFilm(1L, "Старый фильм", 1950);
        createFilm(2L, "Средний фильм", 2000);
        createFilm(3L, "Новый фильм", 2023);

        // ШАГ 2: Ищем фильмы с 1990 по 2010 год
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setYearFrom(1990);
        criteria.setYearTo(2010);

        List<FilmDto> found = filmService.findAllByCriteria(criteria);

        // ШАГ 3: Проверяем что нашёлся только 1 фильм (2000 года)
        assertEquals(1, found.size(), "Должен найтись 1 фильм");
        assertEquals("Средний фильм", found.get(0).getNameRu());
        assertEquals(2000, found.get(0).getYear());

        System.out.println("✅ Тест пройден: поиск по году работает");
    }

    // ============================================
    // ТЕСТ 4: Поиск по рейтингу
    // ============================================
    @Test
    void shouldFindFilmsByRating() {
        // ШАГ 1: Создаём фильмы с разными рейтингами
        createFilmWithRating(1L, "Плохой фильм", 5.0);
        createFilmWithRating(2L, "Средний фильм", 7.0);
        createFilmWithRating(3L, "Хороший фильм", 9.0);

        // ШАГ 2: Ищем только хорошие фильмы (рейтинг >= 8.0)
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setRatingFrom(8.0);
        criteria.setRatingTo(10.0);

        List<FilmDto> found = filmService.findAllByCriteria(criteria);

        // ШАГ 3: Проверяем
        assertEquals(1, found.size(), "Должен найтись 1 фильм");
        assertEquals("Хороший фильм", found.get(0).getNameRu());
        assertTrue(found.get(0).getRatingKinopoisk() >= 8.0);

        System.out.println("✅ Тест пройден: поиск по рейтингу работает");
    }

    // ============================================
    // ТЕСТ 5: Поиск по жанру
    // ============================================
    @Test
    void shouldFindFilmsByGenre() {
        // ШАГ 1: Создаём жанры
        Genre comedy = createGenre("комедия", 1);
        Genre drama = createGenre("драма", 2);

        // ШАГ 2: Создаём фильмы разных жанров
        createFilmWithGenre(1L, "Комедийный фильм 1", comedy);
        createFilmWithGenre(2L, "Комедийный фильм 2", comedy);
        createFilmWithGenre(3L, "Драматический фильм", drama);

        // ШАГ 3: Ищем только комедии
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setGenres(List.of("комедия"));

        List<FilmDto> found = filmService.findAllByCriteria(criteria);

        // ШАГ 4: Проверяем что нашлись только комедии
        assertEquals(2, found.size(), "Должно найтись 2 комедии");

        for (FilmDto film : found) {
            assertTrue(film.getGenres().contains("комедия"),
                    "Все найденные фильмы должны быть комедиями");
        }

        System.out.println("✅ Тест пройден: поиск по жанру работает");
    }

    // ============================================
    // ТЕСТ 6: Не создаёт дубликаты жанров
    // ============================================
    @Test
    void shouldNotCreateDuplicateGenres() {
        // ШАГ 1: Создаём жанр
        Genre comedy = createGenre("комедия", 1);

        // ШАГ 2: Создаём 2 фильма с ОДИНАКОВЫМ жанром
        createFilmWithGenre(1L, "Фильм 1", comedy);
        createFilmWithGenre(2L, "Фильм 2", comedy);

        // ШАГ 3: Проверяем что жанр ОДИН в БД
        assertEquals(1, genreRep.count(),
                "В БД должен быть только 1 жанр, не должно быть дубликатов");

        System.out.println("✅ Тест пройден: дубликаты не создаются");
    }

    // ============================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ для создания тестовых данных
    // ============================================

    private void createFilm(Long kinopoiskId, String name, Integer year) {
        Film film = new Film();
        film.setKinopoiskId(kinopoiskId);
        film.setNameRu(name);
        film.setYear(year);
        film.setFilmTypes(FilmType.FILM);
        filmService.saveAll(List.of(film));
    }

    private void createFilmWithRating(Long kinopoiskId, String name, Double rating) {
        Film film = new Film();
        film.setKinopoiskId(kinopoiskId);
        film.setNameRu(name);
        film.setRatingKinopoisk(rating);
        film.setYear(2023);
        film.setFilmTypes(FilmType.FILM);
        filmService.saveAll(List.of(film));
    }

    private Genre createGenre(String name, Integer idApi) {
        Genre genre = new Genre();
        genre.setName(name);
        genre.setIdApi(idApi);
        return genreRep.save(genre);
    }

    private void createFilmWithGenre(Long kinopoiskId, String name, Genre genre) {
        Film film = new Film();
        film.setKinopoiskId(kinopoiskId);
        film.setNameRu(name);
        film.setYear(2023);
        film.setFilmTypes(FilmType.FILM);

        Set<Genre> genres = new HashSet<>();
        genres.add(genre);
        film.setGenres(genres);

        filmService.saveAll(List.of(film));
    }
}