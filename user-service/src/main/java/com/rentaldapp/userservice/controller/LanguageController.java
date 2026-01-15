package com.rentaldapp.userservice.controller;

import com.rentaldapp.userservice.model.dto.LanguageDTO;
import com.rentaldapp.userservice.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/languages")

public class LanguageController {

    @Autowired
    private LanguageService languageService;

    @GetMapping("/all")
    public ResponseEntity<List<LanguageDTO>> getAllLanguages() {
        List<LanguageDTO> languages = languageService.getAllLanguages();
        return ResponseEntity.ok(languages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageDTO> getLanguageById(@PathVariable Integer id) {
        LanguageDTO language = languageService.getLanguageById(id);
        return ResponseEntity.ok(language);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LanguageDTO> getLanguageByCode(@PathVariable String code) {
        LanguageDTO language = languageService.getLanguageByCode(code);
        return ResponseEntity.ok(language);
    }
}