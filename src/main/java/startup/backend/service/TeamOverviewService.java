package startup.backend.service;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.client.AuthServiceClient;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.TeamOverviewResponse;
import startup.backend.dto.UserResponse;
import startup.backend.repository.TeamOverviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamOverviewService {

    private final TeamOverviewRepository teamOverviewRepository;
    private final AuthServiceClient authServiceClient;

    @Transactional(readOnly = true)
    public List<TeamOverviewResponse> getTeamOverview() {
        return teamOverviewRepository.aggregateTeamOverview()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TeamOverviewResponse toResponse(Tuple tuple) {
        Long userId = tuple.get("userId", Long.class);

        String name = "User-" + userId;
        String role = "UNKNOWN";

        try {
            ApiResponse<UserResponse> apiResponse = authServiceClient.getUserById(userId);
            UserResponse user = apiResponse != null ? apiResponse.getData() : null;
            if (user != null) {
                name = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                        (user.getLastName() != null ? user.getLastName() : "")).trim();
                if (name.isBlank()) {
                    name = user.getUsername() != null ? user.getUsername() : name;
                }
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    role = user.getRoles().iterator().next();
                }
            }
        } catch (Exception ignored) {
            // fallback to mock-style response if user service is unavailable
        }

        return TeamOverviewResponse.builder()
                .userId(userId)
                .name(name)
                .role(role)
                .totalAssignedTasks(getLong(tuple, "totalAssignedTasks"))
                .openTasksCount(getLong(tuple, "openTasksCount"))
                .inProgressTasksCount(getLong(tuple, "inProgressTasksCount"))
                .inReviewTasksCount(getLong(tuple, "inReviewTasksCount"))
                .completedTasksCount(getLong(tuple, "completedTasksCount"))
                .build();
    }

    private long getLong(Tuple tuple, String alias) {
        Number value = tuple.get(alias, Number.class);
        return value == null ? 0L : value.longValue();
    }
}
