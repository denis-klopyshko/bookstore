package com.bookstore.controller;

import com.bookstore.exception.FileFormatException;
import com.bookstore.service.impl.FileDataUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
public class DataUploadingController {
    private final FileDataUploadService service;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(path = "/csv/upload/{type}")
    public ResponseEntity<Object> loadData(@RequestParam(name = "file") MultipartFile file,
                                           @PathVariable(name = "type") String type) {

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
