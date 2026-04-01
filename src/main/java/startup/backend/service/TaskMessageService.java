package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.Exception.TaskLifecycleException;
import startup.backend.client.AuthServiceClient;
import startup.backend.dto.AddMessageRequest;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.MessageResponse;
import startup.backend.dto.UserResponse;
import startup.backend.entity.Task;
import startup.backend.entity.TaskMessage;
import startup.backend.enums.NotificationType;
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
    private final NotificationService notificationService;
    private final AuthServiceClient authServiceClient;   // ✅ ADDED

    // ---------------- ADD COMMENT ----------------

    public MessageResponse addMessage(Long taskId, AddMessageRequest request, Long senderId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> TaskLifecycleException.taskAlreadyClosed(
                        "Task not found with ID: " + taskId));

        if (task.getStatus() == TaskStatus.COMPLETE) {
            throw TaskLifecycleException.taskAlreadyClosed(
                    "Cannot add messages to a CLOSED task");
        }

        TaskMessage message = TaskMessage.builder()
                .taskId(task.getId())
                .message(request.getMessage())
                .senderId(senderId)
                .build();

        TaskMessage saved = taskMessageRepository.save(message);

        notificationService.createNotification(
                task.getAssignedTo(),
                task.getId(),
                "New comment added on task: " + task.getTitle(),
                NotificationType.COMMENT_ADDED,
                ""
        );

        // ✅ Resolve sender name — FeignClientConfig forwards Authorization header automatically
        String senderName = resolveSenderName(senderId);

        return mapToResponse(saved, senderName);
    }

    // ---------------- GET COMMENTS ----------------

    public List<MessageResponse> getMessagesByTaskId(Long taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw TaskLifecycleException.taskAlreadyClosed(
                    "Task not found with ID: " + taskId);
        }

        return taskMessageRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(msg -> {
                    String senderName = resolveSenderName(msg.getSenderId());
                    return mapToResponse(msg, senderName);
                })
                .collect(Collectors.toList());
    }

    // ---------------- HELPERS ----------------

    /**
     * Calls auth-service to get firstName + lastName.
     * FeignClientConfig already forwards the JWT — no manual header needed.
     * Falls back to "User {id}" if the call fails for any reason.
     */
    private String resolveSenderName(Long userId) {
        if (userId == null) return "Unknown";
        try {
            ApiResponse<UserResponse> response = authServiceClient.getUserById(userId);
            if (response != null && response.getData() != null) {
                UserResponse user = response.getData();
                String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                String lastName  = user.getLastName()  != null ? user.getLastName()  : "";
                String fullName  = (firstName + " " + lastName).trim();
                return fullName.isBlank() ? "User " + userId : fullName;
            }
        } catch (Exception e) {
            // Auth service unreachable or returned error — safe fallback
        }
        return "User " + userId;
    }

    // ---------------- MAPPER ----------------

    private MessageResponse mapToResponse(TaskMessage message, String senderName) {
        return MessageResponse.builder()
                .id(message.getId())
                .taskId(message.getTaskId())
                .message(message.getMessage())
                .senderId(message.getSenderId())
                .senderName(senderName)           // ✅ populated
                .createdAt(message.getCreatedAt())
                .build();
    }
}