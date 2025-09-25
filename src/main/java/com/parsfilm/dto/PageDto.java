package com.parsfilm.dto;

import java.util.List;

public class PageDto {
    private int number;
    private List<FilmDto> films;

    public PageDto(int number, List<FilmDto> films) {
        this.number = number;
        this.films = films;
    }
    public PageDto() {}

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<FilmDto> getFilms() {
        return films;
    }

    public void setFilms(List<FilmDto> films) {
        this.films = films;
    }
}
