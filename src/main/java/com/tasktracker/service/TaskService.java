package com.tasktracker.service;

import com.tasktracker.dto.TaskRequest;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.entity.Project;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.TaskPriority;
import com.tasktracker.entity.TaskStatus;
import com.tasktracker.entity.User;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.ProjectRepository;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Value("${task.default-priority}")
    private String defaultPriority;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority()
                : TaskPriority.valueOf(defaultPriority));
        task.setProject(project);
        task.setDueDate(request.getDueDate());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task saved = taskRepository.save(task);
        return TaskResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(TaskStatus status, Long projectId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (status != null && projectId != null) {
            return taskRepository.findByProjectIdAndStatus(projectId, status, pageable)
                    .map(TaskResponse::fromEntity);
        } else if (status != null) {
            return taskRepository.findByStatus(status, pageable).map(TaskResponse::fromEntity);
        } else if (projectId != null) {
            return taskRepository.findByProjectId(projectId, pageable).map(TaskResponse::fromEntity);
        }
        return taskRepository.findAll(pageable).map(TaskResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        task.setTitle(request.getTitle());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        task.setDueDate(request.getDueDate());

        // allow moving a task to a different project
        if (!task.getProject().getId().equals(request.getProjectId())) {
            Project newProject = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));
            task.setProject(newProject);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        Task updated = taskRepository.save(task);
        return TaskResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", projectId);
        }
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
