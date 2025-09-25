package com.parsfilm.entity.helpEntity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "request_counter")
public class RequestCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_date", nullable = false, unique = true)
    private LocalDate requestDate;

    @Column(name = "requests", nullable = false)
    private Integer requests;

    public RequestCounter() {
    }
    public RequestCounter(LocalDate requestDate, Integer requests) {
        this.requestDate = requestDate;
        this.requests = requests;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public Integer getRequests() {
        return requests;
    }

    public void setRequests(Integer requests) {
        this.requests = requests;
    }
}
