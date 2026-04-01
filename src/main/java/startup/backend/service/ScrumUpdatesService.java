package startup.backend.service;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.ScrumUpdatesResponse;
import startup.backend.repository.ScrumUpdatesRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ScrumUpdatesService {

    private final ScrumUpdatesRepository scrumUpdatesRepository;

    @Transactional(readOnly = true)
    public ScrumUpdatesResponse getScrumUpdates(int days) {
        LocalDateTime windowEnd = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime windowStart = windowEnd.minusDays(days);

        Tuple tuple = scrumUpdatesRepository.fetchScrumUpdateMetrics(windowStart);

        long totalTasks = getLong(tuple, "totalTasks");
        long tasksUpdatedInWindow = getLong(tuple, "updatedInWindow");
        long tasksCompletedInWindow = getLong(tuple, "completedInWindow");
        long tasksInProgress = getLong(tuple, "currentlyInProgress");

        double completionRateInWindow = tasksUpdatedInWindow == 0
                ? 0.0
                : (tasksCompletedInWindow * 100.0) / tasksUpdatedInWindow;

        double inProgressShare = totalTasks == 0
                ? 0.0
                : (tasksInProgress * 100.0) / totalTasks;

        return ScrumUpdatesResponse.builder()
                .rangeDays(days)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .tasksUpdatedInLastXDays(tasksUpdatedInWindow)
                .tasksCompletedInLastXDays(tasksCompletedInWindow)
                .tasksCurrentlyInProgress(tasksInProgress)
                .summaryStatistics(
                        ScrumUpdatesResponse.SummaryStatistics.builder()
                                .totalTasks(totalTasks)
                                .completionRateInWindow(roundTwoDecimals(completionRateInWindow))
                                .inProgressShare(roundTwoDecimals(inProgressShare))
                                .build()
                )
                .build();
    }

    private long getLong(Tuple tuple, String alias) {
        Number value = tuple.get(alias, Number.class);
        return value == null ? 0L : value.longValue();
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
