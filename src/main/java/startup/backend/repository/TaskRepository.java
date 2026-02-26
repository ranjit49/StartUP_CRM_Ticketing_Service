package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startup.backend.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import startup.backend.enums.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByParentId(Long parentId);

    List<Task> findByParentIdIsNull();
	 long countByStatus(TaskStatus status);

    long countByAssignedTo(Long assignedTo);

    List<Task> findTop5ByOrderByUpdatedAtDesc();

    @Query("SELECT t FROM Task t WHERE t.description IS NOT NULL AND TRIM(t.description) <> '' ORDER BY t.createdAt DESC")
    List<Task> findTop5RecentCommentTasks(Pageable pageable);
}


