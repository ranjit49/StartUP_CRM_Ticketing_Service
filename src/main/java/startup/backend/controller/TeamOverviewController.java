package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import startup.backend.dto.TeamOverviewResponse;
import startup.backend.service.TeamOverviewService;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TeamOverviewController {

    private final TeamOverviewService teamOverviewService;

    @GetMapping("/overview")
    public ResponseEntity<List<TeamOverviewResponse>> getTeamOverview() {
        return ResponseEntity.ok(teamOverviewService.getTeamOverview());
    }
}
