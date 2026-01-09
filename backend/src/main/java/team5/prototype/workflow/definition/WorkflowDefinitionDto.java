package team5.prototype.workflow.definition;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team5.prototype.dto.TenantDto;
import team5.prototype.workflow.step.WorkflowStepDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowDefinitionDto {

    private Long id;
    private String name;
    private String description;
    private TenantDto tenant;
    private List<WorkflowStepDto> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
