package startup.backend.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import startup.backend.dto.ParentTaskDto;
import startup.backend.dto.TaskTreeResponseDto;
import startup.backend.entity.Task;
import startup.backend.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskTreeMapper {

    private final TaskRepository taskRepository;

    public TaskTreeResponseDto toTreeDto(Task task) {

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

        List<TaskTreeResponseDto> children = taskRepository
                .findByParentId(task.getId())
                .stream()
                .map(this::toTreeDto)   // recursion
                .collect(Collectors.toList());

        return TaskTreeResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .type(task.getType())
                .assignedTo(task.getAssignedTo())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .parent(parentDto)
                .children(children)
                .build();
    }
}
