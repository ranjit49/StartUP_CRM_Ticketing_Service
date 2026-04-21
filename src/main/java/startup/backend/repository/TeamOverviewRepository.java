package startup.backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TeamOverviewRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Tuple> aggregateTeamOverview() {
        return entityManager.createQuery(
                        """
                        SELECT
                            t.assignedTo AS userId,
                            COUNT(t.id) AS totalAssignedTasks,
                            SUM(CASE WHEN t.status <> startup.backend.enums.TaskStatus.COMPLETE THEN 1 ELSE 0 END) AS openTasksCount,
                            SUM(CASE WHEN t.status = startup.backend.enums.TaskStatus.IN_PROGRESS THEN 1 ELSE 0 END) AS inProgressTasksCount,
                            SUM(CASE WHEN t.status = startup.backend.enums.TaskStatus.IN_REVIEW THEN 1 ELSE 0 END) AS inReviewTasksCount,
                            SUM(CASE WHEN t.status = startup.backend.enums.TaskStatus.COMPLETE THEN 1 ELSE 0 END) AS completedTasksCount
                        FROM Task t
                        WHERE t.assignedTo IS NOT NULL
                        GROUP BY t.assignedTo
                        ORDER BY t.assignedTo
                        """,
                        Tuple.class
                )
                .getResultList();
    }
}
