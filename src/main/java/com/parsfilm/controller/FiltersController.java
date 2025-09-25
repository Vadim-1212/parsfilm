package com.parsfilm.controller;

import com.parsfilm.service.FiltersSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/filters")
public class FiltersController {

    private final FiltersSyncService filtersSyncService;

    public FiltersController(FiltersSyncService filtersSyncService) {
        this.filtersSyncService = filtersSyncService;
    }

    @GetMapping("/sync")
    public ResponseEntity<String> sync() {
        filtersSyncService.syncAll();
        return ResponseEntity.ok("Справочники жанров и стран обновлены");
    }
}
