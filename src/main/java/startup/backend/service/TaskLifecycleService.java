package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.Exception.TaskLifecycleException;
import startup.backend.entity.Task;
import startup.backend.enums.TaskStatus;
import startup.backend.repository.TaskRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskLifecycleService {

    private final TaskRepository taskRepository;

    // ---------------- STATUS UPDATE ----------------

    public void changeStatus(Task task, TaskStatus newStatus) {

        if (task.getStatus() == TaskStatus.CLOSED) {

            throw TaskLifecycleException.taskAlreadyClosed("Closed task cannot be modified");
        }

        validateTransition(task.getStatus(), newStatus);

        // Parent closing rule
        if (newStatus == TaskStatus.CLOSED) {
            validateChildrenClosed(task.getId());
        }

        task.setStatus(newStatus);
    }

    // ---------------- ASSIGNMENT ----------------

    public void assignTask(Task task, Long userId) {

        if (task.getStatus() == TaskStatus.CLOSED) {

            throw TaskLifecycleException.taskAlreadyClosed("Cannot assign a CLOSED task");
        }

        task.setAssignedTo(userId);
    }

    // ---------------- RULES ----------------

    private void validateTransition(TaskStatus current, TaskStatus target) {

        boolean valid = switch (current) {
            case OPEN ->
                    target == TaskStatus.IN_PROGRESS ||
                            target == TaskStatus.BLOCKED;

            case BLOCKED ->
                    target == TaskStatus.IN_PROGRESS;

            case IN_PROGRESS ->
                    target == TaskStatus.RESOLVED;

            case RESOLVED ->
                    target == TaskStatus.CLOSED;

            default -> false;
        };

        if (!valid) {

            throw TaskLifecycleException.invalidStatusTransition(current, target);
        }
    }

    private void validateChildrenClosed(Long parentId) {

        if (hasOpenDescendants(parentId)) {

            throw TaskLifecycleException.parentHasOpenDescendants();
        }
    }

    private boolean hasOpenDescendants(Long parentId) {

        List<Task> children = taskRepository.findByParentId(parentId);

        for (Task child : children) {

            // If this child itself is not closed → stop
            if (child.getStatus() != TaskStatus.CLOSED) {
                return true;
            }

            // Recursively check grandchildren
            if (hasOpenDescendants(child.getId())) {
                return true;
            }
        }

        return false;
    }

}