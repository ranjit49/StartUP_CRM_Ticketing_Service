package startup.backend.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.ScrumUpdatesResponse;
import startup.backend.service.ScrumUpdatesService;

@RestController
@RequestMapping("/scrum")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class ScrumUpdatesController {

    private final ScrumUpdatesService scrumUpdatesService;

    @GetMapping("/updates")
    public ResponseEntity<ScrumUpdatesResponse> getScrumUpdates(
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days
    ) {
        return ResponseEntity.ok(scrumUpdatesService.getScrumUpdates(days));
    }
}
