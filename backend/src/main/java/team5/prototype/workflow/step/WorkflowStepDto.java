package team5.prototype.workflow.step;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team5.prototype.dto.RoleDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowStepDto {

    private Long id;
    private String name;
    private String description;
    private Integer durationHours;
    private Integer sequenceOrder;
    private RoleDto requiredRole;
}
