package com.parsfilm.service;

import com.parsfilm.dto.FilmSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilmSearchCriteriaService {

    // Null better than empty
    @Transactional(readOnly = true)
    public FilmSearchCriteria normalizeCriteria(FilmSearchCriteria c) {

        // убираем null и пустые строки
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
        return c;
    }

}


