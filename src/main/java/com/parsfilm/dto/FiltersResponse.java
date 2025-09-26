package com.parsfilm.dto;


import java.util.List;

public class FiltersResponse {

    private List<GenreItem> genres;

    private List<CountryItem> countries;

    public List<GenreItem> getGenres() { return genres; }
    public void setGenres(List<GenreItem> genres) { this.genres = genres; }

    public List<CountryItem> getCountries() { return countries; }
    public void setCountries(List<CountryItem> countries) { this.countries = countries; }

    public static class GenreItem {
        private Integer id;
        private String genre;   // название жанра

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getGenre() { return genre; }
        public void setGenre(String genre) { this.genre = genre; }
    }

    public static class CountryItem {
        private Integer id;
        private String country; // название страны

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}
