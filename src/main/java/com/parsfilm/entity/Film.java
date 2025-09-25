package com.parsfilm.entity;

import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "films")
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kinopoiskId;
    private String nameRu;
    private String nameEn;
    private String nameOriginal;
    private String posterUrl;
    private String webUrl;
    private Integer year;
    private String description;
    private Double ratingKinopoisk; //"ratingKinopoisk": 7.9,

    @Enumerated(EnumType.STRING)
    private FilmType filmTypes = FilmType.ALL;

    private Integer ratingAgeLimits;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "film_countries",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id")
    )
    private Set<Country> countries = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "film_genres",
            joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    public Film() {}

    public Film(Long kinopoiskId,
                String nameRu,
                String nameEn,
                String nameOriginal,
                String posterUrl,
                String webUrl,
                Integer year,
                String description,
                FilmType filmTypes,
                Integer ratingAgeLimits,
                Set<Country> countries,
                Set<Genre> genres,
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getRatingAgeLimits() {
        return ratingAgeLimits;
    }

    public void setRatingAgeLimits(Integer ratingAgeLimits) {
        this.ratingAgeLimits = ratingAgeLimits;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }
    public Double getRatingKinopoisk() {
        return ratingKinopoisk;
    }
    public void setRatingKinopoisk(Double ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }
    public void addGenre(Genre genre) {
        this.genres.add(genre);
        genre.getFilms().add(this); // двусторонняя связь
    }

    public void addCountry(Country country) {
        this.countries.add(country);
        country.getFilms().add(this); // двусторонняя связь
    }

}

