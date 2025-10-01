package com.parsfilm.service.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parsfilm.dto.FilmDto;
import com.parsfilm.service.EmailService;
import com.parsfilm.service.FilmReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class FilmMessageConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final FilmReportService filmReportService;

    public FilmMessageConsumer(ObjectMapper objectMapper,
                               EmailService emailService,
                               FilmReportService filmReportService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.filmReportService = filmReportService;
    }

    @JmsListener(destination = "${film.queue.name}")
    public void receiveMessage(String messageJson) {
        File zipFile = null;
        try {
            List<FilmDto> films = objectMapper.readValue(messageJson, new TypeReference<>() {});

            if (films.isEmpty()) {
                log.warn("Consumer: получено пустое сообщение");
                return;
            }

            // 1. Используем существующий FilmReportService для создания ZIP
            zipFile = filmReportService.generateReportFiles(films);

            if (zipFile == null) {
                log.error("Не удалось создать ZIP файл с отчетом");
                return;
            }

            // 2. Отправляем email через существующий EmailService
            emailService.sendReportByEmail("Vadim9579@icloud.com", zipFile);

            log.info("Consumer: отправлен email с {} фильмами", films.size());

        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения из очереди", e);
            throw new RuntimeException("Ошибка обработки сообщения: " + e.getMessage());
        } finally {
            // 3. Удаляем временный ZIP файл
            cleanupTempFile(zipFile);
        }
    }

    private void cleanupTempFile(File zipFile) {
        try {
            if (zipFile != null && zipFile.exists()) {
                boolean deleted = zipFile.delete();
                if (deleted) {
                    log.debug("Временный файл удален: {}", zipFile.getName());
                } else {
                    log.warn("Не удалось удалить временный файл: {}", zipFile.getName());
                }
            }
        } catch (Exception e) {
            log.warn("Ошибка при удалении временного файла: {}", zipFile.getName(), e);
        }
    }
}