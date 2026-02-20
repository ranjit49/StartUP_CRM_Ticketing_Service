package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startup.backend.entity.TaskMessage;

import java.util.List;

@Repository
public interface TaskMessageRepository extends JpaRepository<TaskMessage, Long> {

    /**
     * Fetch all messages for a task, ordered by creation time (ASC)
     */
    List<TaskMessage> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
