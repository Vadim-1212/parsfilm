package com.parsfilm.service;

import com.parsfilm.dto.FilmSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FilmSearchCriteriaService {

    @Transactional(readOnly = true)
    public FilmSearchCriteria normalizeCriteria(FilmSearchCriteria c) {
        c.setCountries(cleanList(c.getCountries()));
        c.setGenres(cleanList(c.getGenres()));
        return c;
    }

    private List<String> cleanList(List<String> list) {
        if (list == null) return null;
            return list.stream()
                    .filter(x -> x != null)
                    .map(String::trim)
                    .filter(x -> !x.isEmpty())
                    .toList();
    }
}


