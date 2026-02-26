package startup.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.AddMessageRequest;
import startup.backend.dto.MessageResponse;
import startup.backend.service.TaskMessageService;

import java.util.List;

@RestController
@RequestMapping("/ticket-tasks/{taskId}/messages")
@RequiredArgsConstructor
public class TaskMessageController {

    private final TaskMessageService taskMessageService;

    // ---------------- ADD COMMENT ----------------

    @PostMapping
    public ResponseEntity<MessageResponse> addMessage(
            @PathVariable Long taskId,
            @Valid @RequestBody AddMessageRequest request) {

        Long senderId = getCurrentUserId();

        MessageResponse response =
                taskMessageService.addMessage(taskId, request, senderId);

        return ResponseEntity.ok(response);
    }

    // ---------------- GET COMMENTS ----------------

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                taskMessageService.getMessagesByTaskId(taskId)
        );
    }

    // ---------------- HELPER ----------------

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return (Long) principal;
    }
}
