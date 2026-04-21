package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.client.AuthServiceClient;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.ChatMessageDto;
import startup.backend.dto.SendMessageRequest;
import startup.backend.dto.UserResponse;
import startup.backend.entity.ChatMessage;
import startup.backend.repository.ChatMessageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final AuthServiceClient authServiceClient;

    public ChatMessageDto sendMessage(Long senderId, SendMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .message(request.getMessage())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getConversation(Long userId1, Long userId2) {
        return chatMessageRepository.findConversation(userId1, userId2)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long senderId, Long receiverId) {
        chatMessageRepository
                .findConversation(senderId, receiverId)
                .stream()
                .filter(m -> m.getReceiverId().equals(receiverId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    chatMessageRepository.save(m);
                });
    }

    private ChatMessageDto mapToDto(ChatMessage msg) {
        String senderName  = resolveName(msg.getSenderId());
        String receiverName = resolveName(msg.getReceiverId());

        return ChatMessageDto.builder()
                .id(msg.getId())
                .senderId(msg.getSenderId())
                .senderName(senderName)
                .receiverId(msg.getReceiverId())
                .receiverName(receiverName)
                .message(msg.getMessage())
                .createdAt(msg.getCreatedAt())
                .isRead(msg.isRead())
                .build();
    }

    private String resolveName(Long userId) {
        if (userId == null) return "Unknown";
        try {
            ApiResponse<UserResponse> response = authServiceClient.getUserById(userId);
            if (response != null && response.getData() != null) {
                UserResponse user = response.getData();
                String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                String lastName  = user.getLastName()  != null ? user.getLastName()  : "";
                String full = (firstName + " " + lastName).trim();
                return full.isBlank() ? "User " + userId : full;
            }
        } catch (Exception ignored) {}
        return "User " + userId;
    }
}