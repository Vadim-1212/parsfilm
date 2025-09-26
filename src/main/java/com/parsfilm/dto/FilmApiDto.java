package com.parsfilm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Класс дто, принимать первичный поиск по фильтру
public class FilmApiDto {
    private Long kinopoiskId;
    private String nameRu;
    private String nameEn;
    private String nameOriginal;
    private String posterUrl;
    private String webUrl;
    private Integer year;
    private String description;

    @JsonProperty("type")
    private FilmType filmTypes;

    private String ratingAgeLimits;
    private Double ratingKinopoisk;

    // Вместо List<String> - объекты как приходят с API
    private List<CountryApi> countries = new ArrayList<>();
    private List<GenreApi> genres = new ArrayList<>();

    // Вложенные классы для объектов API
    public static class CountryApi {
        private String country;

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class GenreApi {
        private String genre;

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
    }

    // Все остальные геттеры и сеттеры...


    public Long getKinopoiskId() {
        return kinopoiskId;
    }

    public void setKinopoiskId(Long kinopoiskId) {
        this.kinopoiskId = kinopoiskId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameOriginal() {
        return nameOriginal;
    }

    public void setNameOriginal(String nameOriginal) {
        this.nameOriginal = nameOriginal;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FilmType getFilmTypes() {
        return filmTypes;
    }

    public void setFilmTypes(FilmType filmTypes) {
        this.filmTypes = filmTypes;
    }

    public List<GenreApi> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreApi> genres) {
        if (genres == null) {
            this.genres = new ArrayList<>();
            return;
        }
        this.genres = genres.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<CountryApi> getCountries() {
        return countries;
    }

    public void setCountries(List<CountryApi> countries) {
        if (countries == null) {
            this.countries = new ArrayList<>();
            return;
        }
        this.countries = countries.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public String getRatingAgeLimits() {
        return ratingAgeLimits;
    }

    public void setRatingAgeLimits(String ratingAgeLimits) {
        this.ratingAgeLimits = ratingAgeLimits;
    }

    public Double getRatingKinopoisk() {
        return ratingKinopoisk;
    }

    public void setRatingKinopoisk(Double ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }

    // Метод для конвертации в FilmDto
    public FilmDto toFilmDto() {
        FilmDto dto = new FilmDto();
        dto.setKinopoiskId(this.kinopoiskId);
        dto.setNameRu(this.nameRu);
        dto.setNameEn(this.nameEn);
        dto.setNameOriginal(this.nameOriginal);
        dto.setPosterUrl(this.posterUrl);
        dto.setWebUrl(this.webUrl);
        dto.setYear(this.year);
        dto.setDescription(this.description);
        dto.setFilmTypes(this.filmTypes);
        dto.setRatingAgeLimits(this.ratingAgeLimits);
        dto.setRatingKinopoisk(this.ratingKinopoisk);

        // Конвертируем объекты в строки
        dto.setCountries(this.countries == null ? Collections.emptyList() : this.countries.stream()
                .filter(Objects::nonNull)
                .map(CountryApi::getCountry)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        dto.setGenres(this.genres == null ? Collections.emptyList() : this.genres.stream()
                .filter(Objects::nonNull)
                .map(GenreApi::getGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return dto;
    }
}
