package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.Exception.CustomAccessDeniedException;
import startup.backend.Exception.TaskLifecycleException;
import startup.backend.client.AuthServiceClient;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.CreateTaskRequest;
import startup.backend.dto.TaskResponse;
import startup.backend.dto.UserResponse;
import startup.backend.entity.Task;
import startup.backend.enums.NotificationType;
import startup.backend.enums.TaskStatus;
import startup.backend.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskLifecycleService lifecycleService;
    private final NotificationService notificationService;
    private final AuthServiceClient authServiceClient;  // ✅ ADDED

    // ---------------- CREATE TASK ----------------

    public TaskResponse createTask(CreateTaskRequest request, Long userId) {
        Task parent = null;

        if (request.getParentId() != null) {
            parent = taskRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent task not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description("")
                .priority(request.getPriority())
                .status(request.getStatus())
                .dueDate(request.getDueDate())
                .type(request.getType())
                .parentId(parent != null ? parent.getId() : null)
                .createdBy(userId)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    // ---------------- GET BY ID ----------------

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> TaskLifecycleException
                        .taskAlreadyClosed("Task not found with ID: " + id));
        return mapToResponse(task);
    }

    // ---------------- UPDATE STATUS ----------------

    @Transactional
    public TaskResponse updateTaskStatus(Long id, Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskLifecycleException
                        .taskAlreadyClosed("Task not found"));

        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(status);
        } catch (Exception e) {
            throw TaskLifecycleException.invalidStatusTransition(task.getStatus(), null);
        }

        // ✅ Fixed bug: was getAssignedTo().equals(null) which never works
        if (task.getAssignedTo() == null) {
            throw new CustomAccessDeniedException(
                    "You are not allowed to modify this status until you assign this task"
            );
        }

        lifecycleService.changeStatus(task, newStatus);

        Task saved = taskRepository.save(task);

        notificationService.createNotification(
                task.getAssignedTo(),
                task.getId(),
                "Task status updated to: " + task.getStatus(),
                NotificationType.STATUS_CHANGED,
                ""
        );

        return mapToResponse(saved);
    }

    // ---------------- ASSIGN TASK ----------------

    @Transactional
    public TaskResponse assignTask(Long taskId, Long assignedTo) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskLifecycleException
                        .taskAlreadyClosed("Task not found"));

        lifecycleService.assignTask(task, assignedTo);

        Task saved = taskRepository.save(task);

        notificationService.createNotification(
                assignedTo,
                task.getId(),
                "You have been assigned to task: " + task.getTitle(),
                NotificationType.TASK_ASSIGNED,
                ""
        );

        return mapToResponse(saved);
    }

    // ---------------- GET CHILD TASKS ----------------

    public List<TaskResponse> getChildTasks(Long parentId) {
        if (!taskRepository.existsById(parentId)) {
            throw TaskLifecycleException.taskAlreadyClosed("Parent task not found");
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
                .toList();

        // ✅ Resolve names — FeignClientConfig forwards JWT automatically
        String createdByName  = resolveName(task.getCreatedBy());
        String assignedToName = resolveName(task.getAssignedTo());

        return TaskResponse.builder()
                .id(task.getId())
                .parentId(task.getParentId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .type(task.getType())
                .assignedTo(task.getAssignedTo())
                .assignedToName(assignedToName)   // ✅
                .createdBy(task.getCreatedBy())
                .createdByName(createdByName)     // ✅
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .children(childResponses)
                .build();
    }

    // ---------------- NAME RESOLVER ----------------

    /**
     * Fetches firstName + lastName from auth-service.
     * FeignClientConfig forwards Authorization header — no manual token needed.
     * Falls back gracefully if auth-service is unavailable or userId is null.
     */
    private String resolveName(Long userId) {
        if (userId == null) return null;
        try {
            ApiResponse<UserResponse> response = authServiceClient.getUserById(userId);
            if (response != null && response.getData() != null) {
                UserResponse user = response.getData();
                String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                String lastName  = user.getLastName()  != null ? user.getLastName()  : "";
                String fullName  = (firstName + " " + lastName).trim();
                return fullName.isBlank() ? "User " + userId : fullName;
            }
        } catch (Exception ignored) {
            // Auth service unreachable — safe fallback
        }
        return "User " + userId;
    }
}