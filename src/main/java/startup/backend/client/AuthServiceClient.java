package startup.backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.UserResponse;

import java.util.List;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface AuthServiceClient {

    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);

    // ✅ ADD THIS — matches the auth-service endpoint exactly
    @GetMapping("/api/v1/users")
    ApiResponse<List<UserResponse>> getAllUsers();
}