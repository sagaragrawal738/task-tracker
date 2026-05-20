package com.tasktracker.service;

import com.tasktracker.dto.TaskRequest;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.entity.*;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.ProjectRepository;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Project sampleProject;
    private User sampleUser;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(taskService, "defaultPriority", "MEDIUM");

        sampleProject = new Project("Backend API", "Backend service");
        sampleProject.setId(1L);
        sampleProject.setCreatedAt(LocalDateTime.now());

        sampleUser = new User("Alice Johnson", "alice@example.com", "DEVELOPER");
        sampleUser.setId(1L);

        sampleTask = new Task("Implement login", TaskStatus.TODO, TaskPriority.HIGH, sampleProject);
        sampleTask.setId(1L);
        sampleTask.setAssignee(sampleUser);
        sampleTask.setDueDate(LocalDate.of(2026, 6, 15));
    }

    @Test
    void createTask_shouldReturnCreatedTask() {
        TaskRequest request = new TaskRequest("Implement login", TaskStatus.TODO, TaskPriority.HIGH,
                1L, 1L, LocalDate.of(2026, 6, 15));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals("Implement login", result.getTitle());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(1L, result.getProjectId());
    }

    @Test
    void createTask_withoutPriority_shouldUseDefault() {
        TaskRequest request = new TaskRequest("Write tests", null, null, 1L, null, null);

        Task savedTask = new Task("Write tests", TaskStatus.TODO, TaskPriority.MEDIUM, sampleProject);
        savedTask.setId(2L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse result = taskService.createTask(request);

        assertEquals(TaskPriority.MEDIUM, result.getPriority());
    }

    @Test
    void createTask_projectNotFound_shouldThrowException() {
        TaskRequest request = new TaskRequest("Task", null, null, 99L, null, null);
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(request));
    }

    @Test
    void createTask_assigneeNotFound_shouldThrowException() {
        TaskRequest request = new TaskRequest("Task", null, null, 1L, 99L, null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(request));
    }

    @Test
    void getTaskById_shouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        TaskResponse result = taskService.getTaskById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Implement login", result.getTitle());
        assertEquals("Alice Johnson", result.getAssigneeName());
    }

    @Test
    void getTaskById_notFound_shouldThrowException() {
        when(taskRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(50L));
    }

    @Test
    void getAllTasks_noFilters_shouldReturnAllPaged() {
        Page<Task> page = new PageImpl<>(List.of(sampleTask));
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<TaskResponse> result = taskService.getAllTasks(null, null, 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllTasks_filterByStatus_shouldReturnFiltered() {
        Page<Task> page = new PageImpl<>(List.of(sampleTask));
        when(taskRepository.findByStatus(eq(TaskStatus.TODO), any(Pageable.class))).thenReturn(page);

        Page<TaskResponse> result = taskService.getAllTasks(TaskStatus.TODO, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(TaskStatus.TODO, result.getContent().get(0).getStatus());
    }

    @Test
    void getAllTasks_filterByProjectId_shouldReturnFiltered() {
        Page<Task> page = new PageImpl<>(List.of(sampleTask));
        when(taskRepository.findByProjectId(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<TaskResponse> result = taskService.getAllTasks(null, 1L, 0, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateTask_shouldReturnUpdatedTask() {
        TaskRequest request = new TaskRequest("Implement login v2", TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH, 1L, 1L, LocalDate.of(2026, 7, 1));

        Task updatedTask = new Task("Implement login v2", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, sampleProject);
        updatedTask.setId(1L);
        updatedTask.setAssignee(sampleUser);
        updatedTask.setDueDate(LocalDate.of(2026, 7, 1));

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskResponse result = taskService.updateTask(1L, request);

        assertEquals("Implement login v2", result.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
    }

    @Test
    void deleteTask_shouldDeleteSuccessfully() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_notFound_shouldThrowException() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(99L));
    }

    @Test
    void getTasksByProject_shouldReturnTasks() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(sampleTask));

        List<TaskResponse> result = taskService.getTasksByProject(1L);

        assertEquals(1, result.size());
        assertEquals("Implement login", result.get(0).getTitle());
    }

    @Test
    void getTasksByProject_projectNotFound_shouldThrowException() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByProject(99L));
    }
}
