package startup.backend.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.ChatMessageDto;
import startup.backend.dto.SendMessageRequest;
import startup.backend.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        Long senderId = getCurrentUserId();
        return ResponseEntity.ok(chatService.sendMessage(senderId, request));
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<ChatMessageDto>> getConversation(
            @PathVariable Long otherUserId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(
                chatService.getConversation(currentUserId, otherUserId)
        );
    }

    @PatchMapping("/read/{senderId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long senderId) {
        Long currentUserId = getCurrentUserId();
        chatService.markAsRead(senderId, currentUserId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        Object details = auth.getDetails();
        if (!(details instanceof Claims claims)) {
            throw new RuntimeException("JWT claims not found");
        }
        Object idObj = claims.get("id");
        if (idObj instanceof Integer i) return i.longValue();
        if (idObj instanceof Long l)    return l;
        if (idObj instanceof String s)  return Long.parseLong(s);
        throw new RuntimeException("User ID not found in token");
    }
}