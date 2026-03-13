package startup.backend.service;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.ProjectDetailsResponse;
import startup.backend.repository.ProjectDetailsRepository;

@Service
@RequiredArgsConstructor
public class ProjectDetailsService {

    private final ProjectDetailsRepository projectDetailsRepository;

    @Value("${project.details.name:Startup CRM Ticketing}")
    private String projectName;

    @Value("${project.details.owner:Engineering Team}")
    private String projectOwner;

    @Transactional(readOnly = true)
    public ProjectDetailsResponse getProjectDetails() {
        Tuple tuple = projectDetailsRepository.fetchProjectTaskMetrics();

        long totalTasks = getLong(tuple, "totalTasks");
        long completedTasks = getLong(tuple, "completedTasks");
        long inProgressTasks = getLong(tuple, "inProgressTasks");
        long openTasks = getLong(tuple, "openTasks");

        double progressPercentage = totalTasks == 0
                ? 0.0
                : (completedTasks * 100.0) / totalTasks;

        return ProjectDetailsResponse.builder()
                .projectName(projectName)
                .projectOwner(projectOwner)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .openTasks(openTasks)
                .progressPercentage(roundTwoDecimals(progressPercentage))
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
