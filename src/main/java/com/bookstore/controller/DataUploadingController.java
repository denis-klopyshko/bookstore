package com.bookstore.controller;

import com.bookstore.exception.FileFormatException;
import com.bookstore.service.FileDataUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
public class DataUploadingController {
    private final FileDataUploadService service;

    @PostMapping(path = "/csv/upload/{type}")
    public ResponseEntity<Object> loadData(@RequestParam("file") MultipartFile file, @PathVariable String type) {

        if (!Objects.equals(file.getContentType(), "text/csv")) {
            throw new FileFormatException("Not a csv file!");
        }

        Map<String, Runnable> mapping = new HashMap<>();
        mapping.put("books", () -> service.processBooksFile(file));
        mapping.put("users", () -> service.processUsersFile(file));
        mapping.put("ratings", () -> service.processRatingsFile(file));

        if (!mapping.containsKey(type)) {
            return ResponseEntity.notFound().build();
        }

        mapping.get(type).run();
        return ResponseEntity.noContent().build();
    }
}
