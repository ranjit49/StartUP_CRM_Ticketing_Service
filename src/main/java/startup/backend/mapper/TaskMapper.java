package startup.backend.mapper;

import org.springframework.stereotype.Component;
import startup.backend.dto.ParentTaskDto;
import startup.backend.dto.TaskResponseDto;
import startup.backend.entity.Task;
import startup.backend.repository.TaskRepository;

@Component
public class TaskMapper {

    private final TaskRepository taskRepository;

    public TaskMapper(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponseDto toDto(Task task) {

        ParentTaskDto parentDto = null;

        if (task.getParentId() != null) {
            Task parent = taskRepository.findById(task.getParentId()).orElse(null);
            if (parent != null) {
                parentDto = new ParentTaskDto(
                        parent.getId(),
                        parent.getTitle(),
                        parent.getStatus().name(),
                        parent.getType().name()
                );
            }
        }

        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .type(task.getType())
                .assignedTo(task.getAssignedTo())
                .createdBy(task.getCreatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .parent(parentDto)
                .build();
    }
}
