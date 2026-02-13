package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParentTaskDto {
    private Long id;
    private String title;
    private String status;
    private String type;
}
