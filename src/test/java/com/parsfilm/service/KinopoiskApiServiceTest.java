package com.parsfilm.service;

import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;
import com.parsfilm.repository.RequestCounterRep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class KinopoiskApiServiceTest {

    private KinopoiskApiService apiService;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder().build();
        RequestCounterRep mockRepo = mock(RequestCounterRep.class);
        RequestCounterService counterService = new RequestCounterService(mockRepo);
        apiService = new KinopoiskApiService(webClient, counterService);

        // Устанавливаем значения через рефлексию, т.к. они @Value
        ReflectionTestUtils.setField(apiService, "baseUrl", "https://kinopoiskapiunofficial.tech/api/v2.2/films");
        ReflectionTestUtils.setField(apiService, "apiKey", "test-key");
    }

    @Test
    void shouldBuildUrlWithoutCountriesAndGenres() {
        // Arrange
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setYearFrom(2000);
        criteria.setYearTo(2020);
        criteria.setRatingFrom(7.0);
        criteria.setType(FilmType.FILM);

        // Act
        List<String> urls = apiService.buildUrl(criteria);

        // Assert
        assertEquals(1, urls.size());
        String url = urls.get(0);
        assertTrue(url.contains("yearFrom=2000"));
        assertTrue(url.contains("yearTo=2020"));
        assertTrue(url.contains("ratingFrom=7.0"));
        assertTrue(url.contains("type=FILM"));
    }

    @Test
    void shouldBuildMultipleUrlsForCountriesAndGenres() {
        // Arrange
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setCountryIds(List.of(1, 2));  // 2 страны
        criteria.setGenreIds(List.of(10, 20));   // 2 жанра

        // Act
        List<String> urls = apiService.buildUrl(criteria);

        // Assert
        // 2 страны × 2 жанра = 4 комбинации
        assertEquals(4, urls.size());
        assertTrue(urls.stream().anyMatch(u -> u.contains("countries=1") && u.contains("genres=10")));
        assertTrue(urls.stream().anyMatch(u -> u.contains("countries=1") && u.contains("genres=20")));
        assertTrue(urls.stream().anyMatch(u -> u.contains("countries=2") && u.contains("genres=10")));
        assertTrue(urls.stream().anyMatch(u -> u.contains("countries=2") && u.contains("genres=20")));
    }

    @Test
    void shouldBuildUrlsOnlyForCountries() {

        // Arrange
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setCountryIds(List.of(1, 2, 3));

        // Act
        List<String> urls = apiService.buildUrl(criteria);

        // Assert
        assertEquals(3, urls.size());

    }

    @Test
    void shouldBuildUrlsOnlyForGenres() {
        // Arrange
        FilmSearchCriteria criteria = new FilmSearchCriteria();
        criteria.setGenreIds(List.of(10, 20));

        // Act
        List<String> urls = apiService.buildUrl(criteria);

        // Assert
        assertEquals(2, urls.size());
    }
}
