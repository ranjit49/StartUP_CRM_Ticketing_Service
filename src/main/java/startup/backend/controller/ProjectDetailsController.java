package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import startup.backend.dto.ProjectDetailsResponse;
import startup.backend.service.ProjectDetailsService;

@RestController
@RequestMapping("/ticket-tasks/project")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectDetailsController {

    private final ProjectDetailsService projectDetailsService;

    @GetMapping("/details")
    public ResponseEntity<ProjectDetailsResponse> getProjectDetails() {
        return ResponseEntity.ok(projectDetailsService.getProjectDetails());
    }
}
