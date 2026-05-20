package com.tasktracker.dto;

import com.tasktracker.entity.TaskPriority;
import com.tasktracker.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private TaskStatus status;

    private TaskPriority priority;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId;

    private LocalDate dueDate;

    public TaskRequest() {
    }

    public TaskRequest(String title, TaskStatus status, TaskPriority priority,
                       Long projectId, Long assigneeId, LocalDate dueDate) {
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
