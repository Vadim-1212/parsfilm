package com.parsfilm.repository;

import com.parsfilm.entity.helpEntity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRep extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);
    Optional<Genre> findByIdApi(Integer idApi);
    Optional<Genre> findByNameIgnoreCase(String name);
}
