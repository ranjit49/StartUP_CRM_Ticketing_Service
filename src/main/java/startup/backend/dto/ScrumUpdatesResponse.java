package startup.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScrumUpdatesResponse {

    private int rangeDays;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private long tasksUpdatedInLastXDays;
    private long tasksCompletedInLastXDays;
    private long tasksCurrentlyInProgress;
    private SummaryStatistics summaryStatistics;

    @Getter
    @Builder
    public static class SummaryStatistics {
        private long totalTasks;
        private double completionRateInWindow;
        private double inProgressShare;
    }
}
