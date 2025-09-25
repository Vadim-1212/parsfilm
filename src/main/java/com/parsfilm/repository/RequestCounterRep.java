package com.parsfilm.repository;

import com.parsfilm.entity.helpEntity.RequestCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestCounterRep extends JpaRepository<RequestCounter, Long> {

    // вернёт первую запись по id
    Optional<RequestCounter> findFirstByOrderByIdAsc();
}
