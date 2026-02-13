package startup.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.CreateTaskRequest;
import startup.backend.dto.TaskResponse;
import startup.backend.service.TaskService;

import java.util.List;

@RestController
@RequestMapping("/ticket-tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ---------------- CREATE ----------------

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        Long userId = getCurrentUserId();

        TaskResponse response = taskService.createTask(request, userId);

        return ResponseEntity.ok(response);
    }

    // ---------------- GET BY ID ----------------

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // ---------------- GET CHILD TASKS ----------------

    @GetMapping(params = "parentId")
    public ResponseEntity<List<TaskResponse>> getChildTasks(
            @RequestParam Long parentId) {

        return ResponseEntity.ok(taskService.getChildTasks(parentId));
    }

    // ---------------- GET ROOT TASKS ----------------

    @GetMapping(params = "root")
    public ResponseEntity<List<TaskResponse>> getRootTasks(
            @RequestParam Boolean root) {

        if (Boolean.TRUE.equals(root)) {
            return ResponseEntity.ok(taskService.getRootTasks());
        }

        return ResponseEntity.badRequest().build();
    }

    // ---------------- HELPER ----------------

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return (Long) principal;
    }
}
