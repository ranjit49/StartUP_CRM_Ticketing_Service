package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.Exception.TaskLifecycleException;
import startup.backend.dto.AddMessageRequest;
import startup.backend.dto.MessageResponse;
import startup.backend.entity.Task;
import startup.backend.entity.TaskMessage;
import startup.backend.enums.TaskStatus;
import startup.backend.repository.TaskMessageRepository;
import startup.backend.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskMessageService {

    private final TaskRepository taskRepository;
    private final TaskMessageRepository taskMessageRepository;

    // ---------------- ADD COMMENT ----------------

    public MessageResponse addMessage(Long taskId, AddMessageRequest request, Long senderId) {

        // Validate task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskLifecycleException.taskAlreadyClosed("Task not found with ID: " + taskId));

        if (task.getStatus() == TaskStatus.CLOSED) {
            throw TaskLifecycleException.taskAlreadyClosed("Cannot add messages to a CLOSED task");
        }

        // Create message entity
        TaskMessage message = TaskMessage.builder()
                .taskId(task.getId())
                .message(request.getMessage())
                .senderId(senderId)
                .build();

        TaskMessage saved = taskMessageRepository.save(message);

        return mapToResponse(saved);
    }

    // ---------------- GET COMMENTS ----------------

    public List<MessageResponse> getMessagesByTaskId(Long taskId) {

        // Validate task exists
        if (!taskRepository.existsById(taskId)) {
            throw TaskLifecycleException.taskAlreadyClosed("Task not found with ID: " + taskId);
        }

        return taskMessageRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------- MAPPER ----------------

    private MessageResponse mapToResponse(TaskMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .taskId(message.getTaskId())
                .message(message.getMessage())
                .senderId(message.getSenderId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
