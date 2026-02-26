package startup.backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import startup.backend.config.FeignClientConfig;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.UserResponse;

@FeignClient(
        name = "STARTUP-AUTHENTICATION-SERVICE",
        configuration = FeignClientConfig.class
)
public interface AuthServiceClient {

    @GetMapping("/api/v1/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);

}
