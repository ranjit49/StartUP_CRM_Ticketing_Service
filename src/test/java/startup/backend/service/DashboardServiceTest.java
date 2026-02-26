package startup.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import startup.backend.dto.DashboardSummaryResponse;
import startup.backend.entity.Task;
import startup.backend.enums.TaskPriority;
import startup.backend.enums.TaskStatus;
import startup.backend.enums.TaskType;
import startup.backend.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getSummary_shouldReturnAggregatedDashboardData() {
        Task task = Task.builder()
                .id(10L)
                .title("Fix login issue")
                .description("User cannot reset password")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .type(TaskType.TICKET)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();

        when(taskRepository.count()).thenReturn(20L);
        when(taskRepository.countByStatus(TaskStatus.OPEN)).thenReturn(8L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(6L);
        when(taskRepository.countByStatus(TaskStatus.CLOSED)).thenReturn(4L);
        when(taskRepository.countByAssignedTo(77L)).thenReturn(3L);
        when(taskRepository.findTop5ByOrderByUpdatedAtDesc()).thenReturn(List.of(task));
        when(taskRepository.findTop5RecentCommentTasks(any(PageRequest.class))).thenReturn(List.of(task));

        DashboardSummaryResponse response = dashboardService.getSummary(77L);

        assertEquals(20L, response.getTotalTasks());
        assertEquals(8L, response.getTotalOpenTasks());
        assertEquals(6L, response.getTotalInProgressTasks());
        assertEquals(4L, response.getTotalCompletedTasks());
        assertEquals(3L, response.getMyAssignedTasksCount());
        assertEquals(1, response.getRecentActivities().size());
        assertEquals(1, response.getRecentComments().size());
        assertEquals("Fix login issue", response.getRecentActivities().get(0).getTaskTitle());
        assertEquals("User cannot reset password", response.getRecentComments().get(0).getComment());

        verify(taskRepository).findTop5RecentCommentTasks(PageRequest.of(0, 5));
    }
}
