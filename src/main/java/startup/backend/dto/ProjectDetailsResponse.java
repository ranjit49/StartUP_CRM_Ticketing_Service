package startup.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectDetailsResponse {
    private String projectName;
    private String projectOwner;
    private long totalTasks;
    private long completedTasks;
    private long inProgressTasks;
    private long openTasks;
    private double progressPercentage;
}
