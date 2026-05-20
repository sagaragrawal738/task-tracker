package com.tasktracker.controller;

import com.tasktracker.config.AppConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    private final AppConfig appConfig;

    public InfoController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getAppInfo() {
        Map<String, String> info = Map.of(
                "name", appConfig.getAppName(),
                "version", appConfig.getAppVersion()
        );
        return ResponseEntity.ok(info);
    }
}
