package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.entity.Task;
import startup.backend.enums.TaskStatus;
import startup.backend.enums.TaskType;
import startup.backend.repository.TaskRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCreationService {

    private final TaskRepository taskRepository;

    /**
     * Create root ticket
     */
    public Task createRootTicket(Task task) {

        task.setId(null);
        task.setParentId(null);
        task.setType(TaskType.TICKET);
        task.setStatus(TaskStatus.OPEN);

        return taskRepository.save(task);
    }

    /**
     * Create child task under ANY task (ticket or subtask)
     */
    @Transactional
    public Task createChildTask(Long parentTaskId, Task childTask) {

        Task parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Parent task not found")
                );

        // Rule 1: Parent must not be CLOSED
        if (parent.getStatus() == TaskStatus.CLOSED) {
            throw new IllegalStateException("Cannot create child task under CLOSED task");
        }

        childTask.setId(null);
        childTask.setParentId(parentTaskId);
        childTask.setType(TaskType.SUBTASK);
        childTask.setStatus(TaskStatus.OPEN);

        return taskRepository.save(childTask);
    }

    /**
     * Bulk child creation
     */
    @Transactional
    public List<Task> createChildTasks(Long parentTaskId, List<Task> childTasks) {

        Task parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Parent task not found")
                );

        if (parent.getStatus() == TaskStatus.CLOSED) {
            throw new IllegalStateException("Cannot create child tasks under CLOSED task");
        }

        for (Task task : childTasks) {
            task.setId(null);
            task.setParentId(parentTaskId);
            task.setType(TaskType.SUBTASK);
            task.setStatus(TaskStatus.OPEN);
        }

        return taskRepository.saveAll(childTasks);
    }
}
