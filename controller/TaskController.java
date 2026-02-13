package startup.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.*;
import startup.backend.entity.Task;
import startup.backend.enums.TaskType;
import startup.backend.mapper.TaskMapper;
import startup.backend.mapper.TaskTreeMapper;
import startup.backend.repository.TaskRepository;
import startup.backend.service.TaskCreationService;
import startup.backend.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskCreationService taskCreationService;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskTreeMapper taskTreeMapper;

    @PostMapping
    public TaskResponseDto createRootTask(@Valid @RequestBody CreateTaskRequest request) {

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .assignedTo(request.getAssignedTo())
                .build();

        return taskMapper.toDto(
                taskCreationService.createRootTicket(task)
        );
    }

    @PostMapping("/{parentId}/children")
    public TaskResponseDto createChildTask(
            @PathVariable Long parentId,
            @Valid @RequestBody CreateTaskRequest request
    ) {

        Task child = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .assignedTo(request.getAssignedTo())
                .build();

        return taskMapper.toDto(
                taskCreationService.createChildTask(parentId, child)
        );
    }

    @PatchMapping("/{id}/status")
    public TaskResponseDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return taskMapper.toDto(
                taskService.updateTaskStatus(id, request.getStatus())
        );
    }

    @GetMapping("/{id}")
    public TaskResponseDto getTask(@PathVariable Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return taskMapper.toDto(task);
    }

    @GetMapping("/{id}/tree")
    public TaskTreeResponseDto getTaskTree(@PathVariable Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return taskTreeMapper.toTreeDto(task);
    }
    @PatchMapping("/{id}/assign")
    public TaskResponseDto assignTask(
            @PathVariable Long id,
            @Valid @RequestBody AssignTaskRequest request
    ) {
        return taskMapper.toDto(
                taskService.assignTask(id, request.getAssignedTo())
        );
    }

}
