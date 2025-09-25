package com.parsfilm.service;

import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.GenreRep;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FilmSearchCriteriaService {

    @Transactional(readOnly = true)
    public FilmSearchCriteria normalizeCriteria(FilmSearchCriteria c) {
        if (c.getGenres() != null) {
            c.setGenres(
                    c.getGenres().stream()
                            .filter(g -> g != null && !g.trim().isEmpty())
                            .map(String::trim)
                            .toList()
            );
        }

        if (c.getCountries() != null) {
            c.setCountries(
                    c.getCountries().stream()
                            .filter(ct -> ct != null && !ct.trim().isEmpty())
                            .map(String::trim)
                            .toList()
            );
        }

        // --- Минимальные исправления ---
        if (c.getYearFrom() != null && c.getYearFrom() == 1500) {
            c.setYearFrom(null);
        }
        if (c.getYearTo() != null && c.getYearTo() == LocalDate.now().getYear()) {
            c.setYearTo(null);
        }
        if (c.getRatingFrom() != null && c.getRatingFrom() == 0.0) {
            c.setRatingFrom(null);
        }
        if (c.getRatingTo() != null && c.getRatingTo() == 10.0) {
            c.setRatingTo(null);
        }
        if (c.getType() == null) {
            c.setType(FilmType.ALL);
        }
        // --- конец фикса ---

        return c;
    }

}


