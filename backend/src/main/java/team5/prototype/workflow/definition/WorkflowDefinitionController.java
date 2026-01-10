package team5.prototype.workflow.definition;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-definitions")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService workflowDefinitionService;

    public WorkflowDefinitionController(WorkflowDefinitionService workflowDefinitionService) {
        this.workflowDefinitionService = workflowDefinitionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDefinitionDto> getWorkflowDefinition(@PathVariable Long id) {
        WorkflowDefinitionDto dto = workflowDefinitionService.getWorkflowDefinitionByIdAsDto(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<WorkflowDefinitionDto>> getAllWorkflowDefinitions() {
        List<WorkflowDefinitionDto> dtos = workflowDefinitionService.getAllWorkflowDefinitionsAsDto();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<WorkflowDefinitionDto> createWorkflowDefinition(@RequestBody WorkflowDefinition definition) {
        WorkflowDefinitionDto dto = workflowDefinitionService.createWorkflowDefinitionAsDto(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

}