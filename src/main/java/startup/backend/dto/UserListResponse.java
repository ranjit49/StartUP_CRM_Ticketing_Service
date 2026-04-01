package startup.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserListResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String username;
}