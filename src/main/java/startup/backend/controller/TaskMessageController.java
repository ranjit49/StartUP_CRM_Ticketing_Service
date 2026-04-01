package startup.backend.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.AddMessageRequest;
import startup.backend.dto.MessageResponse;
import startup.backend.service.TaskMessageService;

import java.util.List;

@RestController
@RequestMapping("/ticket-tasks/{taskId}/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        // ✅ Details holds the JWT Claims object — same pattern as TaskController
        Object details = authentication.getDetails();
        if (!(details instanceof Claims claims)) {
            throw new RuntimeException("JWT claims not found in authentication details");
        }

        Object idObj = claims.get("id");
        if (idObj instanceof Integer i) return i.longValue();
        if (idObj instanceof Long l)    return l;
        if (idObj instanceof String s)  return Long.parseLong(s);

        throw new RuntimeException("User ID not found in token");
    }
}
