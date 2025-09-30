package com.parsfilm.service;


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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        System.out.println(">>> Критерии поиска:");
        System.out.println("  yearFrom: " + filmSearchCriteria.getYearFrom());
        System.out.println("  yearTo: " + filmSearchCriteria.getYearTo());
        System.out.println("  ratingFrom: " + filmSearchCriteria.getRatingFrom());
        System.out.println("  ratingTo: " + filmSearchCriteria.getRatingTo());
        System.out.println("  type: " + filmSearchCriteria.getType());
        System.out.println("  genres: " + filmSearchCriteria.getGenres());
        System.out.println("  countries: " + filmSearchCriteria.getCountries());
        System.out.println("  keyword: " + filmSearchCriteria.getKeyword());

        TypedQuery<Film> query = buildCriteriaQuery(filmSearchCriteria);
        List<Film> films = query.getResultList();
        System.out.println(">>> SQL запрос вернул: " + films.size() + " фильмов");
        return films.stream()
                .map(filmMapper::toDto)
                .toList();
    }

    // сбор запроса в бд по критериям от юзера
    private TypedQuery<Film> buildCriteriaQuery(FilmSearchCriteria filmSearchCriteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Film> cq = cb.createQuery(Film.class);
        cq.distinct(true);
        Root<Film> root = cq.from(Film.class);

        Fetch<Film, Country> countryFetch = root.fetch("countries", JoinType.LEFT);
        Fetch<Film, Genre> genreFetch = root.fetch("genres", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        addYearPredicates(predicates, cb, root, filmSearchCriteria);
        addRatingPredicates(predicates, cb, root, filmSearchCriteria);
        addTypePredicates(predicates, cb, root, filmSearchCriteria);
        addKeywordPredicates(predicates, cb, root, filmSearchCriteria);
        addGenreAndCountryPredicates(predicates, cb, root, filmSearchCriteria);

        System.out.println(">>> Всего предикатов: " + predicates.size());
        for (int i = 0; i < predicates.size(); i++) {
            System.out.println(">>> Предикат " + i + ": " + predicates.get(i));
        }

        applySorting(predicates, cb, root, filmSearchCriteria, cq);
        return entityManager.createQuery(cq);
    }

    private void addYearPredicates(List<Predicate> predicates,
                                   CriteriaBuilder cb,
                                   Root<Film> root,
                                   FilmSearchCriteria filmSearchCriteria) {
        if (filmSearchCriteria.getYearFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("year"), filmSearchCriteria.getYearFrom()));
        }
        if (filmSearchCriteria.getYearTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("year"), filmSearchCriteria.getYearTo()));
        }
    }

    private void addRatingPredicates(List<Predicate> predicates,
                                     CriteriaBuilder cb,
                                     Root<Film> root,
                                     FilmSearchCriteria filmSearchCriteria) {
        if (filmSearchCriteria.getRatingFrom() != null) {
            Predicate ratingGreater = cb.greaterThanOrEqualTo(root.get("ratingKinopoisk"), filmSearchCriteria.getRatingFrom());
            predicates.add(cb.or(cb.isNull(root.get("ratingKinopoisk")), ratingGreater));
        }
        if (filmSearchCriteria.getRatingTo() != null) {
            Predicate ratingLess = cb.lessThanOrEqualTo(root.get("ratingKinopoisk"), filmSearchCriteria.getRatingTo());
            predicates.add(cb.or(cb.isNull(root.get("ratingKinopoisk")), ratingLess));
        }
    }

    private void addTypePredicates(List<Predicate> predicates,
                                     CriteriaBuilder cb,
                                     Root<Film> root,
                                     FilmSearchCriteria filmSearchCriteria) {
        FilmType requestedType = filmSearchCriteria.getType();
        if (requestedType != null && requestedType != FilmType.ALL) {
            predicates.add(cb.equal(root.get("filmTypes"), requestedType));
        }
    }

    private void addKeywordPredicates(List<Predicate> predicates,
                                   CriteriaBuilder cb,
                                   Root<Film> root,
                                   FilmSearchCriteria filmSearchCriteria) {
        if (filmSearchCriteria.getKeyword() != null && !filmSearchCriteria.getKeyword().isBlank()) {
            String keyword = "%" + filmSearchCriteria.getKeyword().toLowerCase() + "%";

            Predicate byNameRu = cb.like(cb.lower(root.get("nameRu")), keyword);
            Predicate byNameEn = cb.like(cb.lower(root.get("nameEn")), keyword);
            Predicate byNameOriginal = cb.like(cb.lower(root.get("nameOriginal")), keyword);
            Predicate byDescription = cb.like(cb.lower(root.get("description")), keyword);

            predicates.add(cb.or(byNameRu, byNameEn, byNameOriginal, byDescription));
        }
    }

    private void addGenreAndCountryPredicates(List<Predicate> predicates,
                                              CriteriaBuilder cb,
                                              Root<Film> root,
                                              FilmSearchCriteria filmSearchCriteria) {
        // Только если есть фильтры - тогда делаем JOIN
        if (filmSearchCriteria.getGenres() != null && !filmSearchCriteria.getGenres().isEmpty()) {
            Join<Film, Genre> genreJoin = root.join("genres", JoinType.INNER);
            predicates.add(genreJoin.get("name").in(filmSearchCriteria.getGenres()));
        }

        if (filmSearchCriteria.getCountries() != null && !filmSearchCriteria.getCountries().isEmpty()) {
            Join<Film, Country> countryJoin = root.join("countries", JoinType.INNER);
            predicates.add(countryJoin.get("name").in(filmSearchCriteria.getCountries()));
        }
    }

    private void applySorting(List<Predicate> predicates,
                              CriteriaBuilder cb,
                              Root<Film> root,
                              FilmSearchCriteria filmSearchCriteria,
                              CriteriaQuery<?> cq) {
        cq.where(predicates.toArray(new Predicate[0]));
        if (filmSearchCriteria.getSortBy() != null) {
            jakarta.persistence.criteria.Path<?> sortPath =
                    root.get(filmSearchCriteria.getSortBy().getFieldName());
            if (filmSearchCriteria.getSortOrder() == SortDirection.DESC) {
                cq.orderBy(cb.desc(sortPath));
            } else {
                cq.orderBy(cb.asc(sortPath));
            }
        }
    }




}
