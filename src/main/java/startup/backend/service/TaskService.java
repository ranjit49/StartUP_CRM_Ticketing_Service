package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.entity.Task;
import startup.backend.enums.TaskStatus;
import startup.backend.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskLifecycleService lifecycleService;

    public Task updateTaskStatus(Long taskId, TaskStatus newStatus) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task not found with id: " + taskId)
                );

        lifecycleService.validateStatusChange(task, newStatus);

        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    public Task assignTask(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task not found with id: " + taskId)
                );

        if (task.getStatus() == TaskStatus.CLOSED) {
            throw new IllegalStateException("Cannot assign a CLOSED task");
        }

        task.setAssignedTo(userId);
        return taskRepository.save(task);
    }
}
