package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByParentId(Long parentId);
}
