package com.parsfilm.dto;

import com.parsfilm.entity.Film;
import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.GenreRep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class FilmMapper {

    CountryRep countryRepository;
    GenreRep genreRepository;

    public FilmMapper(CountryRep countryRepository, GenreRep genreRepository) {
        this.countryRepository = countryRepository;
        this.genreRepository = genreRepository;
    }

    public FilmDto toDto(Film film) {
        FilmDto filmDto = new FilmDto();

        filmDto.setCountries(film.getCountries() == null ? List.of() :
                film.getCountries().stream()
                        .map(Country::getName)
                        .toList()
        );
        filmDto.setGenres(film.getGenres() == null ? List.of() :
                film.getGenres().stream()
                        .map(Genre::getName)
                        .toList()
        );

        // правильно берём возрастной рейтинг
        if (film.getRatingAgeLimits() != null) {
            filmDto.setRatingAgeLimits(String.valueOf(film.getRatingAgeLimits()));
        }

        filmDto.setFilmTypes(film.getFilmTypes());
        filmDto.setDescription(film.getDescription());
        filmDto.setYear(film.getYear());
        filmDto.setWebUrl(film.getWebUrl());
        filmDto.setPosterUrl(film.getPosterUrl());
        filmDto.setNameOriginal(film.getNameOriginal());
        filmDto.setNameEn(film.getNameEn());
        filmDto.setNameRu(film.getNameRu());
        filmDto.setKinopoiskId(film.getKinopoiskId());
        filmDto.setRatingKinopoisk(film.getRatingKinopoisk());

        return filmDto;
    }


    public Film toFilm(FilmDto filmDto) {
        Film film = new Film();

        // Страны (null → пустое множество)
        Set<Country> countries = filmDto.getCountries() == null
                ? Set.of()
                : filmDto.getCountries().stream()
                .map(name -> {
                    Country c = new Country();
                    c.setName(name);
                    return c;
                })
                .collect(Collectors.toSet());

        // Жанры (null → пустое множество)
        Set<Genre> genres = filmDto.getGenres() == null
                ? Set.of()
                : filmDto.getGenres().stream()
                .map(name -> {
                    Genre g = new Genre();
                    g.setName(name);
                    return g;
                })
                .collect(Collectors.toSet());

        film.setCountries(countries);
        film.setGenres(genres);

        // преобразуем строку "age16" → 16, из апи приходит строка
        try {
            String raw = filmDto.getRatingAgeLimits();
            if (raw != null) {
                raw = raw.replaceAll("[^0-9]", "");
                if (!raw.isBlank()) {
                    film.setRatingAgeLimits(Integer.valueOf(raw));
                }
            }
        } catch (NumberFormatException e) {
            film.setRatingAgeLimits(null);
        }

        if (filmDto.getFilmTypes() != null) {
            film.setFilmTypes(filmDto.getFilmTypes());
        }
        film.setDescription(filmDto.getDescription());
        film.setYear(filmDto.getYear());
        film.setWebUrl(filmDto.getWebUrl());
        film.setPosterUrl(filmDto.getPosterUrl());
        film.setNameOriginal(filmDto.getNameOriginal());
        film.setNameEn(filmDto.getNameEn());
        film.setNameRu(filmDto.getNameRu());
        film.setKinopoiskId(filmDto.getKinopoiskId());
        film.setRatingKinopoisk(filmDto.getRatingKinopoisk());

        return film;
    }




}
