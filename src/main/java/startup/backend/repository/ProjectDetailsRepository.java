package startup.backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectDetailsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Tuple fetchProjectTaskMetrics() {
        return entityManager.createQuery(
                        """
                        SELECT
                            COUNT(t.id) AS totalTasks,
                            SUM(CASE WHEN t.status = startup.backend.enums.TaskStatus.COMPLETE THEN 1 ELSE 0 END) AS completedTasks,
                            SUM(CASE WHEN t.status = startup.backend.enums.TaskStatus.IN_PROGRESS THEN 1 ELSE 0 END) AS inProgressTasks,
                            SUM(CASE WHEN t.status <> startup.backend.enums.TaskStatus.COMPLETE THEN 1 ELSE 0 END) AS openTasks
                        FROM Task t
                        """,
                        Tuple.class
                )
                .getSingleResult();
    }
}
