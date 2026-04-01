package startup.backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class ScrumUpdatesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Tuple fetchScrumUpdateMetrics(LocalDateTime fromDate) {
        return entityManager.createQuery(
                        """
                        SELECT
                            COUNT(t.id) AS totalTasks,
                            SUM(CASE WHEN t.updatedAt >= :fromDate THEN 1 ELSE 0 END) AS updatedInWindow,
                            SUM(CASE WHEN t.updatedAt >= :fromDate
                                      AND t.status = startup.backend.enums.TaskStatus.COMPLETE THEN 1 ELSE 0 END) AS completedInWindow,
                            SUM(CASE WHEN t.status = startup.backend.enums.TaskStatus.IN_PROGRESS THEN 1 ELSE 0 END) AS currentlyInProgress
                        FROM Task t
                        """,
                        Tuple.class
                )
                .setParameter("fromDate", fromDate)
                .getSingleResult();
    }
}
