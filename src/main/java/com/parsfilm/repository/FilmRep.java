package com.parsfilm.repository;

import com.parsfilm.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FilmRep extends JpaRepository<Film, Long>, JpaSpecificationExecutor<Film> {
    boolean existsByKinopoiskId(Long kinopoiskId);
    Optional<Film> findByKinopoiskId(Long kinopoiskId);


}
