package startup.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamOverviewResponse {
    private Long userId;
    private String name;
    private String role;
    private long totalAssignedTasks;
    private long openTasksCount;
    private long inProgressTasksCount;
    private long completedTasksCount;
}
