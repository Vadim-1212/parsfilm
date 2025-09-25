package com.parsfilm.repository;

import com.parsfilm.entity.helpEntity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRep extends JpaRepository<Country, Long> {
    Optional<Country> findByName(String countryName);
    Optional<Country> findByIdApi(Integer idApi);
    Optional<Country> findByNameIgnoreCase(String name);

}
