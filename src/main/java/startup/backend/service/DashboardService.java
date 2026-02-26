package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.DashboardSummaryResponse;
import startup.backend.dto.RecentActivityResponse;
import startup.backend.dto.RecentCommentResponse;
import startup.backend.entity.Task;
import startup.backend.enums.TaskStatus;
import startup.backend.repository.TaskRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RECENT_LIMIT = 5;

    private final TaskRepository taskRepository;

    public DashboardSummaryResponse getSummary(Long currentUserId) {
        long totalTasks = taskRepository.count();
        long totalOpenTasks = taskRepository.countByStatus(TaskStatus.OPEN);
        long totalInProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long totalCompletedTasks = taskRepository.countByStatus(TaskStatus.CLOSED);
        long myAssignedTasksCount = taskRepository.countByAssignedTo(currentUserId);

        List<RecentActivityResponse> recentActivities = taskRepository.findTop5ByOrderByUpdatedAtDesc()
                .stream()
                .map(this::mapToRecentActivity)
                .toList();

        List<RecentCommentResponse> recentComments = taskRepository
                .findTop5RecentCommentTasks(PageRequest.of(0, RECENT_LIMIT))
                .stream()
                .map(this::mapToRecentComment)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalTasks(totalTasks)
                .totalOpenTasks(totalOpenTasks)
                .totalInProgressTasks(totalInProgressTasks)
                .totalCompletedTasks(totalCompletedTasks)
                .myAssignedTasksCount(myAssignedTasksCount)
                .recentActivities(recentActivities)
                .recentComments(recentComments)
                .build();
    }

    private RecentActivityResponse mapToRecentActivity(Task task) {
        return RecentActivityResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .status(task.getStatus())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private RecentCommentResponse mapToRecentComment(Task task) {
        return RecentCommentResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .comment(task.getDescription())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
