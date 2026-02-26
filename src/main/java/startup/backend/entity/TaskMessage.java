package startup.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Task ID (ticket / subtask / sub-subtask)
     */
    @Column(nullable = false)
    private Long taskId;

    /**
     * Comment message
     */
    @Column(nullable = false, length = 2000)
    private String message;

    /**
     * User ID extracted from JWT (SecurityContext)
     */
    @Column(nullable = false)
    private Long senderId;

    /**
     * Timestamp when comment was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
