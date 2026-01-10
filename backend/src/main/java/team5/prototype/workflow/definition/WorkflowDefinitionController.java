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

    @GetMapping
    public ResponseEntity<List<WorkflowDefinition>> getAllDefinitions() {
        return ResponseEntity.ok(workflowDefinitionService.getAllDefinitions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowDefinition> getDefinitionById(@PathVariable Long id) {
        return workflowDefinitionService.getDefinitionById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkflowDefinition> createWorkflowDefinition(@RequestBody WorkflowDefinition definition) {
        WorkflowDefinition created = workflowDefinitionService.createWorkflowDefinition(definition);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
