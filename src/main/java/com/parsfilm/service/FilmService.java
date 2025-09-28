package com.parsfilm.service;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.parsfilm.dto.*;
import com.parsfilm.entity.Film;
import com.parsfilm.entity.helpEntity.Country;
import com.parsfilm.entity.helpEntity.Genre;
import com.parsfilm.helperClassAndMethods.helperEnums.FilmType;
import com.parsfilm.helperClassAndMethods.helperEnums.SortDirection;
import com.parsfilm.repository.CountryRep;
import com.parsfilm.repository.FilmRep;
import com.parsfilm.repository.GenreRep;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FilmService {

    private FilmRep filmRep;
    private FilmMapper filmMapper;

    private CountryRep countryRep;

    private GenreRep genreRep;


    @PersistenceContext
    private EntityManager entityManager;

    public FilmService(FilmRep filmRep, FilmMapper filmMapper,
                       CountryRep countryRepository,
                       GenreRep genreRepository) {

        this.filmRep = filmRep;
        this.filmMapper = filmMapper;
        this.countryRep = countryRepository;
        this.genreRep = genreRepository;
    }

    @Transactional
    public List<Film> saveAll(List<Film> films) {
        List<Film> saved = new ArrayList<>();
        for (Film film : films) {
            saved.add(saveFilm(film)); // сохраняем каждый фильм через saveFilm
        }
        return saved;
    }

    // сохраняем каждый фильм (жанры и страны) проверяя нет ли уже его в БД
    private Film saveFilm(Film newfilm) {

        Set<Country> attachedCountries = newfilm.getCountries().stream()
                .map(c -> countryRep.findByName(c.getName())
                        .orElseGet(() -> {
                            Country nc = new Country();
                            nc.setName(c.getName());
                            return countryRep.save(nc);
                        }))
                .collect(Collectors.toSet());

        Set<Genre> attachedGenres = newfilm.getGenres().stream()
                .map(g -> genreRep.findByName(g.getName())
                        .orElseGet(() -> {
                            Genre ng = new Genre();
                            ng.setName(g.getName());
                            return genreRep.save(ng);
                        }))
                .collect(Collectors.toSet());

        newfilm.setCountries(attachedCountries);
        newfilm.setGenres(attachedGenres);

        return filmRep.findByKinopoiskId(newfilm.getKinopoiskId())
                .map(existing -> updateFilm(existing, newfilm))
                .orElseGet(() -> filmRep.save(newfilm));
    }

    // сохранить поля у фильма в бд если такого нет
    private Film updateFilm(Film target, Film source) {
        target.setNameRu(source.getNameRu());


        target.setNameEn(source.getNameEn());
        target.setNameOriginal(source.getNameOriginal());
        target.setPosterUrl(source.getPosterUrl());
        target.setWebUrl(source.getWebUrl());
        target.setYear(source.getYear());
        target.setDescription(source.getDescription());
        target.setRatingKinopoisk(source.getRatingKinopoisk());
        target.setFilmTypes(source.getFilmTypes());
        target.setRatingAgeLimits(source.getRatingAgeLimits());

        target.getCountries().clear();
        target.getCountries().addAll(source.getCountries());

        target.getGenres().clear();
        target.getGenres().addAll(source.getGenres());

        return filmRep.save(target);
    }

    // получить все фильмы с бд по критериям
    public List<FilmDto> findAllByCriteria(FilmSearchCriteria filmSearchCriteria) {
        return buildCriteriaQuery(filmSearchCriteria).getResultList().stream()
                .map(filmMapper::toDto)
                .toList();
    }

    // сбор запроса в бд по критериям от юзера
    private TypedQuery<Film> buildCriteriaQuery(FilmSearchCriteria filmSearchCriteria) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Film> cq = cb.createQuery(Film.class);
        // без дублей должно прийти
        cq.distinct(true);
        Root<Film> root = cq.from(Film.class);
        List<Predicate> predicates = new ArrayList<>();

        // Год выпуска
        if (filmSearchCriteria.getYearFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("year"), filmSearchCriteria.getYearFrom()));
        }
        if (filmSearchCriteria.getYearTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("year"), filmSearchCriteria.getYearTo()));
        }

        // Рейтинг
        if (filmSearchCriteria.getRatingFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("ratingKinopoisk"), filmSearchCriteria.getRatingFrom()));
        }
        if (filmSearchCriteria.getRatingTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("ratingKinopoisk"), filmSearchCriteria.getRatingTo()));
        }

        // Тип (FilmType)
        FilmType requestedType = filmSearchCriteria.getType();
        if (requestedType != null && requestedType != FilmType.ALL) {
            predicates.add(cb.equal(root.get("filmTypes"), requestedType));
        }

        // Поиск по ключевому слову
        if (filmSearchCriteria.getKeyword() != null && !filmSearchCriteria.getKeyword().isBlank()) {
            String keyword = "%" + filmSearchCriteria.getKeyword().toLowerCase() + "%";

            Predicate byNameRu = cb.like(cb.lower(root.get("nameRu")), keyword);
            Predicate byNameEn = cb.like(cb.lower(root.get("nameEn")), keyword);
            Predicate byNameOriginal = cb.like(cb.lower(root.get("nameOriginal")), keyword);
            Predicate byDescription = cb.like(cb.lower(root.get("description")), keyword);

            predicates.add(cb.or(byNameRu, byNameEn, byNameOriginal, byDescription));
        }

        // прибавляем к таблице фильмов и таблицы жанров и стран что бы по их полям делять запросы
        // Фильтрация по жанрам (по имени)
        if (filmSearchCriteria.getGenres() != null && !filmSearchCriteria.getGenres().isEmpty()) {
            Join<Film, Genre> genreJoin = root.join("genres");
            predicates.add(genreJoin.get("name").in(filmSearchCriteria.getGenres()));
        }

        // Фильтрация по странам (по имени)
        if (filmSearchCriteria.getCountries() != null && !filmSearchCriteria.getCountries().isEmpty()) {
            Join<Film, Country> countryJoin = root.join("countries");
            predicates.add(countryJoin.get("name").in(filmSearchCriteria.getCountries()));
        }

        // Применяем предикаты
        cq.where(predicates.toArray(new Predicate[0]));

        // Сортировка
        if (filmSearchCriteria.getSortBy() != null) {
            jakarta.persistence.criteria.Path<?> sortPath =
                    root.get(filmSearchCriteria.getSortBy().getFieldName());
            if (filmSearchCriteria.getSortOrder() == SortDirection.DESC) {
                cq.orderBy(cb.desc(sortPath));
            } else {
                cq.orderBy(cb.asc(sortPath));
            }
        }

        return entityManager.createQuery(cq);
    }

    //конвертировать в xml/csv и засунуть в zip
    public File generateReportFiles(List<FilmDto> films) {
        List<FilmDto> sourceFilms = films == null ? List.of() : films;

        ReportDto report = new ReportDto();
        List<PageDto> pages = new ArrayList<>();

        int pageSize = 20;

        // разбить на страницы и собрать в список
        for (int fromIndex = 0; fromIndex < sourceFilms.size(); fromIndex += pageSize) {
            int toIndex = Math.min(fromIndex + pageSize, sourceFilms.size());
            List<FilmDto> pageFilms = new ArrayList<>(sourceFilms.subList(fromIndex, toIndex));
            pages.add(new PageDto(pages.size() + 1, pageFilms));
        }

        report.setPages(pages);

        //создание файлов
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

    // метод что бы добавлять файлы в zip арихив
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
