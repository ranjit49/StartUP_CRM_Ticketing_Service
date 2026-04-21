package startup.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String mobileNo;
    private Set<String> roles;
    private String profileImage;
    private String bio;
    private String location;
    private LocalDateTime createdAt;
    private String status;

}
