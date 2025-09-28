package com.parsfilm.service;

import com.parsfilm.entity.Film;
import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.FilmRep;
import com.parsfilm.repository.GenreRep;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FilmServiceTest {

    @InjectMocks
    private FilmService filmService;

    @Mock
    private FilmRep filmRep;

    @Mock
    private CountryRep countryRep;

    @Mock
    private GenreRep genreRep;

    @Test
    public void saveAllFilmTest() {
//        1. создать объект фильм который пришел
        Film filmNew = new Film(
                12345L,
                "Фильм на русском",
                "Film in Eng",
                "Original film name",
                "This is poster URL",
                "This is web URL",
                1555,
                "Description",
                FilmType.FILM,
                18,
                Set.of(new Country("Россия", new HashSet<Film>(), 1)),
                Set.of(new Genre("Комедии", new HashSet<Film>(), 1)),
                7.7
        );

//        2. Объект БД у которого нужно дополнить поля


        Film filmExisting = new Film(
                12345L,
                "",
                "",
                "",
                "",
                "",
                0,
                "",
                FilmType.FILM,
                18,
                new HashSet<>(),
                new HashSet<>(),
                7.7
        );
//        3. мок на методы вызова
        when(filmRep.findByKinopoiskId(filmNew.getKinopoiskId())).thenReturn(Optional.of(filmExisting));
        when(filmRep.save(any(Film.class))).thenAnswer(a -> a.getArgument(0));

        // имитация что репозтитории жанров и стран что то возращают.
        when(countryRep.findByName(any(String.class)))
                .thenReturn(Optional.of(new Country("Россия", new HashSet<Film>(), 1)));
        when(genreRep.findByName(any(String.class)))
                .thenReturn(Optional.of(new Genre("Комедии", new HashSet<Film>(), 1)));

        //4. Вызвать проверяющий метод
        List<Film> result = filmService.saveAll(List.of(filmNew));

//        5. Сделать проверку сохраненный объект такой же или нет
        Film savedFilm = result.get(0);
        assertEquals(filmNew.getKinopoiskId(), savedFilm.getKinopoiskId());
        assertEquals(filmNew.getNameRu(), savedFilm.getNameRu());
        assertEquals(filmNew.getNameEn(), savedFilm.getNameEn());
        assertEquals(filmNew.getNameOriginal(), savedFilm.getNameOriginal());
        assertEquals(filmNew.getPosterUrl(), savedFilm.getPosterUrl());
        assertEquals(filmNew.getWebUrl(), savedFilm.getWebUrl());
        assertEquals(filmNew.getYear(), savedFilm.getYear());
        assertEquals(filmNew.getDescription(), savedFilm.getDescription());
        assertEquals(filmNew.getFilmTypes(), savedFilm.getFilmTypes());
        assertEquals(filmNew.getRatingAgeLimits(), savedFilm.getRatingAgeLimits());
        assertEquals(filmNew.getGenres(), savedFilm.getGenres());
        assertEquals(filmNew.getCountries(), savedFilm.getCountries());
        assertEquals(filmNew.getRatingKinopoisk(), savedFilm.getRatingKinopoisk());

    }
}
