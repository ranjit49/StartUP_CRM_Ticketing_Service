package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.CreateTaskRequest;
import startup.backend.dto.TaskResponse;
import startup.backend.entity.Task;
import startup.backend.repository.TaskRepository;
import startup.backend.enums.TaskStatus;              


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
	private final TaskLifecycleService lifecycleService;  // >>> ADDED


    // ---------------- CREATE TASK ----------------

    public TaskResponse createTask(CreateTaskRequest request, Long userId) {

        // Validate parent if present
        if (request.getParentId() != null) {
            taskRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent task not found"));
        }
        System.out.println(userId);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .type(request.getType())
                .parentId(request.getParentId())
                .createdBy(userId)
                .build();

        Task saved = taskRepository.save(task);

        return mapToResponse(saved);
    }

    // ---------------- GET BY ID ----------------

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        return mapToResponse(task);
    }
	@Transactional
    public TaskResponse updateTaskStatus(Long taskId, String status) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskStatus newStatus;

        try {
            newStatus = TaskStatus.valueOf(status);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        lifecycleService.changeStatus(task, newStatus);

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }
	 @Transactional
    public TaskResponse assignTask(Long taskId, Long assignedTo) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        lifecycleService.assignTask(task, assignedTo);

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    // ---------------- GET CHILD TASKS ----------------

    public List<TaskResponse> getChildTasks(Long parentId) {

        if (!taskRepository.existsById(parentId)) {
            throw new RuntimeException("Parent task not found");
        }

        return taskRepository.findByParentId(parentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- GET ROOT TASKS ----------------

    public List<TaskResponse> getRootTasks() {
        return taskRepository.findByParentIdIsNull()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- MAPPER ----------------

    private TaskResponse mapToResponse(Task task) {
		List<Task> childTasks = taskRepository.findByParentId(task.getId());

        List<TaskResponse> childResponses = childTasks.stream()
                .map(this::mapToResponse) 
                .toList()
        return TaskResponse.builder()
                .id(task.getId())
                .parentId(task.getParentId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .type(task.getType())
                .assignedTo(task.getAssignedTo())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
			    .children(childResponses)
                .build();
    }
}
