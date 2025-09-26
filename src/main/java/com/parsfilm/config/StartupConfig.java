package com.parsfilm.config;

import com.parsfilm.service.FiltersUpdateService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//При старте приложения и по вызову метода syncAll() загружает из внешнего API
// список жанров и стран и сохраняет их в базу, если их там ещё нет.
@Configuration
public class StartupConfig {

    private final FiltersUpdateService filtersUpdateService;

    public StartupConfig(FiltersUpdateService filtersUpdateService) {
        this.filtersUpdateService = filtersUpdateService;
    }

    @Bean
    ApplicationRunner initOnStartup() {
        return args -> filtersUpdateService.syncAll();
    }
}
