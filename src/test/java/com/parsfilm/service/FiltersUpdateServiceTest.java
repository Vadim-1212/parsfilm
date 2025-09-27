package com.parsfilm.service;

import com.parsfilm.dto.FiltersResponse;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.GenreRep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FiltersUpdateServiceTest {

    @InjectMocks
    private FiltersUpdateService filtersUpdateService;

    @Mock
    private WebClient webClient;

    @Mock
    private GenreRep genreRep;

    @Mock
    private CountryRep countryRep;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;


    //
    @Test
    void syncAll() {

        FiltersResponse filtersResponse = new FiltersResponse();
        FiltersResponse.GenreItem genreItem = new FiltersResponse.GenreItem();
        genreItem.setId(1);
        genreItem.setGenre("Fantasy");
        filtersResponse.setGenres(List.of(genreItem));


        FiltersResponse.CountryItem countryItem = new FiltersResponse.CountryItem();
        countryItem.setId(1);
        countryItem.setCountry("USA");
        filtersResponse.setCountries(List.of(countryItem));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/filters")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FiltersResponse.class))
                .thenReturn(reactor.core.publisher.Mono.just(filtersResponse));

        filtersUpdateService.syncAll();

        verify(genreRep).save(argThat(
                g -> g.getName().equals("Fantasy") && g.getIdApi() == 1 ));

        verify(countryRep).save(argThat(
                с -> с.getName().equals("USA") && с.getIdApi() == 1
        ));
    }
}
