package com.parsfilm.service;

import com.parsfilm.entity.Film;
import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;

import java.util.HashSet;
import java.util.Set;

public class TestDataBuilder {

    public static Film createFilm(Long kinopoiskId, String nameRu) {
        Film film = new Film();
        film.setKinopoiskId(kinopoiskId);
        film.setNameRu(nameRu);
        film.setNameEn(nameRu + " EN");
        film.setYear(2024);
        film.setFilmTypes(FilmType.FILM);
        film.setRatingKinopoisk(7.5);
        film.setCountries(new HashSet<>());
        film.setGenres(new HashSet<>());
        return film;
    }

    public static Country createCountry(String name) {
        Country country = new Country();
        country.setName(name);
        country.setFilms(new HashSet<>());
        return country;
    }

    public static Genre createGenre(String name) {
        Genre genre = new Genre();
        genre.setName(name);
        genre.setFilms(new HashSet<>());
        return genre;
    }

    public static Film createFilmWithGenresAndCountries(
            Long kinopoiskId,
            String nameRu,
            Set<String> genreNames,
            Set<String> countryNames) {

        Film film = createFilm(kinopoiskId, nameRu);

        // Добавляем жанры
        Set<Genre> genres = new HashSet<>();
        for (String name : genreNames) {
            genres.add(createGenre(name));
        }
        film.setGenres(genres);

        // Добавляем страны
        Set<Country> countries = new HashSet<>();
        for (String name : countryNames) {
            countries.add(createCountry(name));
        }
        film.setCountries(countries);

        return film;
    }
}