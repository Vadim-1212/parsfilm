package com.parsfilm.controller;

import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.FilmMapper;
import com.parsfilm.dto.FilmSearchCriteria;
import com.parsfilm.entity.Film;
import com.parsfilm.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/films")
public class FilmController {

    private FilmReportOrchestrationService orchestrationService;
    private EmailService emailService;

    public FilmController(FilmReportOrchestrationService orchestrationService, EmailService emailService)
        {
        this.orchestrationService = orchestrationService;
        this.emailService = emailService;
        }


    @PostMapping("/report-email")
    public ResponseEntity<String> generateAndSendReport(@RequestBody FilmSearchCriteria criteria) {
        String email = criteria.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("email обязателен");
        }

        File zipFile = null;
        try {
            zipFile = orchestrationService.generateReport(criteria);

            if (zipFile == null) {
                return ResponseEntity.internalServerError().body("Ошибка при создании отчёта");
            }

            emailService.sendReportByEmail(email, zipFile);
            return ResponseEntity.ok("Отчёт успешно отправлен на " + email);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        } finally {
            if (zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
        }
    }

}


