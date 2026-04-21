package startup.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserListResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String username;
    private String status;
    private String email;     // ✅ add this
    private String bio;       // ✅ add this
    private String location;  // ✅ add this
    private String mobileNo;  // ✅ add this
    private Set<String> roles; // ✅ add this
}