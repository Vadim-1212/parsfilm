package com.parsfilm.service;

import com.parsfilm.dto.FilmDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceReportTest {

    private File generatedFile;

    @AfterEach
    void cleanup() {
        // Удаляем созданный файл после теста
        if (generatedFile != null && generatedFile.exists()) {
            generatedFile.delete();
        }
    }

    @Test
    void shouldGenerateZipFileWithXmlAndCsv() throws IOException {
        // Arrange - создаём список фильмов
        List<FilmDto> films = createTestFilms();

        FilmReportService filmReportService = new FilmReportService();

        // Act
        generatedFile = filmReportService.generateReportFiles(films);

        // Assert
        assertNotNull(generatedFile, "Файл должен быть создан");
        assertTrue(generatedFile.exists(), "Файл должен существовать");
        assertTrue(generatedFile.getName().endsWith(".zip"), "Файл должен быть ZIP");

        // Проверяем содержимое ZIP
        List<String> filesInZip = getFilesFromZip(generatedFile);
        assertEquals(2, filesInZip.size(), "В ZIP должно быть 2 файла");

        boolean hasXml = filesInZip.stream().anyMatch(name -> name.endsWith(".xml"));
        boolean hasCsv = filesInZip.stream().anyMatch(name -> name.endsWith(".csv"));

        assertTrue(hasXml, "Должен быть XML файл");
        assertTrue(hasCsv, "Должен быть CSV файл");
    }

    @Test
    void shouldGenerateEmptyReportForEmptyList() {
        // Arrange
        List<FilmDto> emptyList = List.of();
        FilmReportService filmReportService = new FilmReportService();


        // Act
        generatedFile = filmReportService.generateReportFiles(emptyList);

        // Assert
        assertNotNull(generatedFile);
        assertTrue(generatedFile.exists());
    }

    // Вспомогательные методы
    private List<FilmDto> createTestFilms() {
        List<FilmDto> films = new ArrayList<>();

        FilmDto film1 = new FilmDto();
        film1.setKinopoiskId(1L);
        film1.setNameRu("Тестовый фильм 1");
        film1.setYear(2020);
        film1.setRatingKinopoisk(8.0);
        film1.setGenres(List.of("Комедия"));
        film1.setCountries(List.of("Россия"));

        films.add(film1);
        return films;
    }

    private List<String> getFilesFromZip(File zipFile) throws IOException {
        List<String> fileNames = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                fileNames.add(entry.getName());
                zis.closeEntry();
            }
        }

        return fileNames;
    }
}