package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private long totalTasks;
    private long totalOpenTasks;
    private long totalInProgressTasks;
    private long totalCompletedTasks;
    private long myAssignedTasksCount;
    private List<RecentActivityResponse> recentActivities;
    private List<RecentCommentResponse> recentComments;
}

