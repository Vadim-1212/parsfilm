package com.parsfilm.service;

import com.parsfilm.dto.FilmSearchCriteria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class FilmSearchCriteriaServiceTest {

    private FilmSearchCriteriaService filmSearchCriteriaService = new FilmSearchCriteriaService();;

    @Test
    void normalizeCriteriaTest() {
        FilmSearchCriteria c = new FilmSearchCriteria();
        c.setGenres(Arrays.asList(" комедии ", "", " ", null, "ужасы"));
        c.setCountries(Arrays.asList(" usa", "france", "", " ", null));

        FilmSearchCriteria result =  filmSearchCriteriaService.normalizeCriteria(c);

        assertThat(result.getCountries())
                .containsExactly("usa", "france");
        assertThat(result.getGenres())
                .containsExactly("комедии", "ужасы");
    }

    @Test
    void normalizeCriteriaTest2() {
        FilmSearchCriteria c = new FilmSearchCriteria();
        c.setCountries(null);
        c.setGenres(null);

        FilmSearchCriteria result =  filmSearchCriteriaService.normalizeCriteria(c);

        assertThat(result.getCountries()).isNull();
        assertThat(result.getGenres()).isNull();
    }

    @Test
    void normalizeCriteriaTest3() {
        FilmSearchCriteria c = new FilmSearchCriteria();
        c.setGenres(List.of("", " ", "   "));
        c.setCountries(List.of("", " ", "   "));

        FilmSearchCriteria result =  filmSearchCriteriaService.normalizeCriteria(c);
        assertThat(result.getCountries()).isEmpty();
        assertThat(result.getGenres()).isEmpty();
    }

}
