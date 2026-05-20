package com.tasktracker.service;

import com.tasktracker.dto.ProjectRequest;
import com.tasktracker.dto.ProjectResponse;
import com.tasktracker.entity.Project;
import com.tasktracker.exception.DuplicateResourceException;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = new Project("Backend API", "Main backend service");
        sampleProject.setId(1L);
        sampleProject.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createProject_shouldReturnSavedProject() {
        ProjectRequest request = new ProjectRequest("Backend API", "Main backend service");
        when(projectRepository.existsByName("Backend API")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

            ProjectResponse result = projectService.createProject(request);

        assertNotNull(result);
        assertEquals("Backend API", result.getName());
        assertEquals("Main backend service", result.getDescription());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_duplicateName_shouldThrowException() {
        ProjectRequest request = new ProjectRequest("Backend API", "Duplicate");
        when(projectRepository.existsByName("Backend API")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> projectService.createProject(request));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void getProjectById_shouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));

        ProjectResponse result = projectService.getProjectById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Backend API", result.getName());
    }

    @Test
    void getProjectById_notFound_shouldThrowException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(99L));
    }

    @Test
    void getAllProjects_shouldReturnPagedResults() {
        Page<Project> page = new PageImpl<>(List.of(sampleProject));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ProjectResponse> result = projectService.getAllProjects(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Backend API", result.getContent().get(0).getName());
    }

    @Test
    void updateProject_shouldReturnUpdatedProject() {
        ProjectRequest request = new ProjectRequest("Updated Name", "Updated desc");
        Project updatedProject = new Project("Updated Name", "Updated desc");
        updatedProject.setId(1L);
        updatedProject.setCreatedAt(sampleProject.getCreatedAt());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(projectRepository.findByName("Updated Name")).thenReturn(Optional.empty());
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);

        ProjectResponse result = projectService.updateProject(1L, request);

        assertEquals("Updated Name", result.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_notFound_shouldThrowException() {
        ProjectRequest request = new ProjectRequest("Whatever", null);
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(99L, request));
    }

    @Test
    void deleteProject_shouldDeleteSuccessfully() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        projectService.deleteProject(1L);

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void deleteProject_notFound_shouldThrowException() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(99L));
    }
}
