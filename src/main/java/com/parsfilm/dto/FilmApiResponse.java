package com.parsfilm.dto;

import java.util.List;

public class FilmApiResponse {
    private int total;
    private int totalPages;
    private List<FilmApiDto> items;

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<FilmApiDto> getItems() {
        return items;
    }

    public void setItems(List<FilmApiDto> items) {
        this.items = items;
    }
}
