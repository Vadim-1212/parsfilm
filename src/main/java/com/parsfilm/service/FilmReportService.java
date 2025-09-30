package com.parsfilm.service;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.parsfilm.dto.FilmDto;
import com.parsfilm.dto.PageDto;
import com.parsfilm.dto.ReportDto;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FilmReportService {

    public File generateReportFiles(List<FilmDto> films) {
        List<FilmDto> sourceFilms = films == null ? List.of() : films;

        ReportDto report = new ReportDto();
        List<PageDto> pages = new ArrayList<>();

        int pageSize = 20;

        for (int fromIndex = 0; fromIndex < sourceFilms.size(); fromIndex += pageSize) {
            int toIndex = Math.min(fromIndex + pageSize, sourceFilms.size());
            List<FilmDto> pageFilms = new ArrayList<>(sourceFilms.subList(fromIndex, toIndex));
            pages.add(new PageDto(pages.size() + 1, pageFilms));
        }

        report.setPages(pages);

        Path xmlPath = null;
        Path csvPath = null;
        Path zipPath = null;

        try {
            String uniqueId = LocalDateTime.now()
                                      .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                              + "-" + UUID.randomUUID().toString().replace("-", "");
            String baseFileName = "films-report-" + uniqueId;

            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            xmlPath = tempDir.resolve(baseFileName + ".xml");
            csvPath = tempDir.resolve(baseFileName + ".csv");
            zipPath = tempDir.resolve(baseFileName + ".zip");

            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.writeValue(xmlPath.toFile(), report);

            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.schemaFor(FilmDto.class).withHeader();
            List<FilmDto> allFilms = new ArrayList<>(sourceFilms);
            csvMapper.writer(csvSchema).writeValue(csvPath.toFile(), allFilms);

            try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                addFileToZip(zos, xmlPath.toFile(), xmlPath.getFileName().toString());
                addFileToZip(zos, csvPath.toFile(), csvPath.getFileName().toString());
            }

            return zipPath.toFile();

        } catch (Exception e) {
            System.out.println("Ошибка при создании файла " + e.getMessage());
            if (zipPath != null) {
                try {
                    Files.deleteIfExists(zipPath);
                } catch (IOException ignored) {
                }
            }
            return null;
        } finally {
            if (xmlPath != null) {
                try {
                    Files.deleteIfExists(xmlPath);
                } catch (IOException ignored) {
                }
            }
            if (csvPath != null) {
                try {
                    Files.deleteIfExists(csvPath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zos.putNextEntry(zipEntry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }

        zos.closeEntry();
    }
}