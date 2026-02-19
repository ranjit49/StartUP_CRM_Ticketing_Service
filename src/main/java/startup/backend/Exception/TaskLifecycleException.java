package startup.backend.Exception;

import startup.backend.enums.TaskStatus;

public class TaskLifecycleException extends RuntimeException {

    public enum ErrorType {
        TASK_ALREADY_CLOSED,
        INVALID_STATUS_TRANSITION,
        PARENT_HAS_OPEN_DESCENDANTS
    }

    private final ErrorType errorType;

    private TaskLifecycleException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public static TaskLifecycleException taskAlreadyClosed(String message) {
        return new TaskLifecycleException(ErrorType.TASK_ALREADY_CLOSED, message);
    }

    public static TaskLifecycleException invalidStatusTransition(TaskStatus current, TaskStatus target) {
        return new TaskLifecycleException(
                ErrorType.INVALID_STATUS_TRANSITION,
                "Invalid status transition: " + current + " -> " + target
        );
    }

    public static TaskLifecycleException parentHasOpenDescendants() {
        return new TaskLifecycleException(
                ErrorType.PARENT_HAS_OPEN_DESCENDANTS,
                "Parent task cannot be CLOSED until ALL subtasks are CLOSED"
        );
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
