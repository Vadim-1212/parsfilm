package com.parsfilm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;

import java.util.ArrayList;
import java.util.List;

// Принимаем то что хочет увидеть User
public class FilmDto {

    private Long kinopoiskId;
    private String nameRu;
    private String nameEn;
    private String nameOriginal;
    private String posterUrl;
    private String webUrl;
    private Integer year;
    private String description;
    private FilmType filmTypes;
    private String ratingAgeLimits;
    private List<String> countries = new ArrayList<>();
    private List<String> genres = new ArrayList<>();
    private Double ratingKinopoisk;


    public FilmDto() {
    }

    public FilmDto(Long kinopoiskId,
                   String nameRu,
                   String nameEn,
                   String nameOriginal,
                   String posterUrl,
                   String webUrl,
                   Integer year,
                   String description,
                   FilmType filmTypes,
                   String ratingAgeLimits,
                   List<String> countries,
                   List<String> genres,
                   Double ratingKinopoisk) {
        this.kinopoiskId = kinopoiskId;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
        this.nameOriginal = nameOriginal;
        this.posterUrl = posterUrl;
        this.webUrl = webUrl;
        this.year = year;
        this.description = description;
        this.filmTypes = filmTypes;
        this.ratingAgeLimits = ratingAgeLimits;
        this.countries = countries;
        this.genres = genres;
        this.ratingKinopoisk = ratingKinopoisk;
    }

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

    public String getRatingAgeLimits() {
        return ratingAgeLimits;
    }

    public void setRatingAgeLimits(String ratingAgeLimits) {
        this.ratingAgeLimits = ratingAgeLimits;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
    public Double getRatingKinopoisk() {
        return ratingKinopoisk;
    }
    public void setRatingKinopoisk(Double ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }
}


