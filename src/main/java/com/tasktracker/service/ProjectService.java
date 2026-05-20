package com.tasktracker.service;

import com.tasktracker.dto.ProjectRequest;
import com.tasktracker.dto.ProjectResponse;
import com.tasktracker.entity.Project;
import com.tasktracker.exception.DuplicateResourceException;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Project with name '" + request.getName() + "' already exists");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project saved = projectRepository.save(project);
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return projectRepository.findAll(pageable).map(ProjectResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        // check for name conflict with a different project
        projectRepository.findByName(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Project with name '" + request.getName() + "' already exists"
                    );
                });

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project updated = projectRepository.save(project);
        return ProjectResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }
        projectRepository.deleteById(id);
    }
}
