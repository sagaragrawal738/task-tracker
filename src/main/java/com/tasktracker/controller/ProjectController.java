package com.tasktracker.controller;

import com.tasktracker.config.AppConfig;
import com.tasktracker.dto.ProjectRequest;
import com.tasktracker.dto.ProjectResponse;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.service.ProjectService;
import com.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final AppConfig appConfig;

    public ProjectController(ProjectService projectService,
                             TaskService taskService,
                             AppConfig appConfig) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.appConfig = appConfig;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size) {
        int pageSize = (size != null) ? size : appConfig.getDefaultPageSize();
        return ResponseEntity.ok(projectService.getAllProjects(page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                         @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTasksByProject(id));
    }
}
