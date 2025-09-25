package com.parsfilm.dto;

import com.parsfilm.helperClassAndMethods.helperEnums.SortBy;
import com.parsfilm.helperClassAndMethods.helperEnums.SortDirection;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Используется только для формирования запроса к Кинопоиск API.
public class FilmSearchCriteria {


    private Integer yearFrom = 1500;
    private Integer yearTo = LocalDate.now().getYear();
    private Double ratingFrom = 0.0;
    private Double ratingTo = 10.0;

    private SortBy sortBy = SortBy.YEAR;             // YEAR, RATING, NUM_VOTE
    private SortDirection sortOrder = SortDirection.DESC; // ASC, DESC

    private List<String> genres = new ArrayList<>();     // ввод пользователя (названия жанров)
    private List<String> countries = new ArrayList<>();  // ввод пользователя (названия стран)

    private List<Integer> genreIds = new ArrayList<>();  // idApi → для API
    private List<Integer> countryIds = new ArrayList<>();// idApi → для API

    private FilmType type = FilmType.ALL;   // FILM, TV_SHOW, TV_SERIES, MINI_SERIES, ALL
    private String keyword = "";             // поиск по названию
    private Integer page = 1; // какую страницу выдать в запросе
    private String email = "";

    public FilmSearchCriteria() {
    }

    public FilmSearchCriteria(Integer yearFrom,
                              Integer yearTo,
                              Double ratingFrom,
                              Double ratingTo,
                              SortBy sortBy,
                              SortDirection sortOrder,
                              List<String> genres,
                              List<String> countries,
                              List<Integer> genreIds,
                              List<Integer> countryIds,
                              FilmType type,
                              String keyword,
                              Integer page,
                              String email
                              ) {
        this.yearFrom = yearFrom;
        this.yearTo = yearTo;
        this.ratingFrom = ratingFrom;
        this.ratingTo = ratingTo;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.genres = genres;
        this.countries = countries;
        this.genreIds = genreIds;
        this.countryIds = countryIds;
        this.type = type;
        this.keyword = keyword;
        this.page = page;
        this.email = email;
    }

    public Integer getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(Integer yearFrom) {
        this.yearFrom = yearFrom;
    }

    public Integer getYearTo() {
        return yearTo;
    }

    public void setYearTo(Integer yearTo) {
        this.yearTo = yearTo;
    }

    public Double getRatingFrom() {
        return ratingFrom;
    }

    public void setRatingFrom(Double ratingFrom) {
        this.ratingFrom = ratingFrom;
    }

    public Double getRatingTo() {
        return ratingTo;
    }

    public void setRatingTo(Double ratingTo) {
        this.ratingTo = ratingTo;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public SortDirection getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortDirection sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public List<Integer> getCountryIds() {
        return countryIds;
    }

    public void setCountryIds(List<Integer> countryIds) {
        this.countryIds = countryIds;
    }

    public void addGenreId(Integer genreId) {
        this.genreIds.add(genreId);
    }

    public void addCountryId(Integer countryId) {
        this.countryIds.add(countryId);
    }

    public FilmType getType() {
        return type;
    }

    public void setType(FilmType type) {
        this.type = type;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

}

