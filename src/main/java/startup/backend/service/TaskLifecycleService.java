package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.entity.Task;
import startup.backend.enums.TaskStatus;
import startup.backend.repository.TaskRepository;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskLifecycleService {

    private final TaskRepository taskRepository;

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    TaskStatus.OPEN, EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.BLOCKED),
                    TaskStatus.BLOCKED, EnumSet.of(TaskStatus.IN_PROGRESS),
                    TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.RESOLVED),
                    TaskStatus.RESOLVED, EnumSet.of(TaskStatus.CLOSED)
            );

    public void validateStatusChange(Task task, TaskStatus newStatus) {

        // Rule 1: CLOSED task is immutable
        if (task.getStatus() == TaskStatus.CLOSED) {
            throw new IllegalStateException("CLOSED task cannot be updated");
        }

        // Rule 2: Allowed transitions only
        Set<TaskStatus> allowedNext =
                ALLOWED_TRANSITIONS.getOrDefault(task.getStatus(), Set.of());

        if (!allowedNext.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + task.getStatus() + " to " + newStatus
            );
        }

        // Rule 3: Parent-child consistency
        if (newStatus == TaskStatus.CLOSED) {
            validateAllChildrenClosed(task.getId());
        }
    }

    private void validateAllChildrenClosed(Long parentTaskId) {

        List<Task> children = taskRepository.findByParentId(parentTaskId);

        for (Task child : children) {
            if (child.getStatus() != TaskStatus.CLOSED) {
                throw new IllegalStateException(
                        "Task cannot be CLOSED while child tasks are still open"
                );
            }
            // 🔁 recursive check
            validateAllChildrenClosed(child.getId());
        }
    }
}


