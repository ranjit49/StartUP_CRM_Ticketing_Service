package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.Exception.CustomAccessDeniedException;
import startup.backend.Exception.TaskLifecycleException;
import startup.backend.dto.CreateTaskRequest;
import startup.backend.dto.TaskResponse;
import startup.backend.entity.Task;
import startup.backend.enums.NotificationType;
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
    private final NotificationService notificationService;

    // ---------------- CREATE TASK ----------------

    public TaskResponse createTask(CreateTaskRequest request, Long userId) {

        Task parent = null;
        System.out.println("createTask api (TaskService.java) is started !");

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
        System.out.println("createTask api (TaskService.java) is at the end !");

        return mapToResponse(saved);
    }

    // ---------------- GET BY ID ----------------

    public TaskResponse getTaskById(Long id) {
        System.out.println("getTaskById api (TaskService.java) is started !");
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> TaskLifecycleException.taskAlreadyClosed("Task not found with ID: " + id));
        System.out.println("getTaskById api (TaskService.java) is ended !");
        return mapToResponse(task);
    }
	@Transactional
    public TaskResponse updateTaskStatus(Long id, Long taskId, String status) {
        System.out.println("updateTaskStatus api (TaskService.java) is started !");
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskLifecycleException.taskAlreadyClosed("Task not found"));

        TaskStatus newStatus;

        try {
            newStatus = TaskStatus.valueOf(status);
        } catch (Exception e) {
            throw TaskLifecycleException.invalidStatusTransition(task.getStatus(), null);
        }

        lifecycleService.changeStatus(task, newStatus);
        if (task.getAssignedTo().equals(null)) {
            throw new CustomAccessDeniedException(
                    "You are not allowed to modify this status until you assign this task"
            );
        }

        Task saved = taskRepository.save(task);
        notificationService.createNotification(
                task.getAssignedTo(),
                task.getId(),
                "Task status updated to: " + task.getStatus(),
                NotificationType.STATUS_CHANGED,
                ""
        );
        System.out.println("updateTaskStatus api (TaskService.java) is ended !");
        return mapToResponse(saved);
    }
	 @Transactional
    public TaskResponse assignTask(Long taskId, Long assignedTo) {
         System.out.println("assignTask api (TaskService.java) is started !");
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskLifecycleException.taskAlreadyClosed("Task not found"));

        lifecycleService.assignTask(task, assignedTo);

        Task saved = taskRepository.save(task);
         notificationService.createNotification(
                 assignedTo,
                 task.getId(),
                 "You have been assigned to task: " + task.getTitle(),
                 NotificationType.TASK_ASSIGNED,
                 ""
         );
         System.out.println("assignTask api (TaskService.java) is ended !");
         return mapToResponse(saved);
    }

    // ---------------- GET CHILD TASKS ----------------

    public List<TaskResponse> getChildTasks(Long parentId) {
        System.out.println("getChildTasks api (TaskService.java) is ended !");

        if (!taskRepository.existsById(parentId)) {
            throw TaskLifecycleException.taskAlreadyClosed("Parent task not found");
        }
        System.out.println("getChildTasks api (TaskService.java) is ended !");
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
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
			    .children(childResponses)
                .build();
    }
}
