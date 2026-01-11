package team5.prototype.workflow.definition;

import org.springframework.stereotype.Service;
import team5.prototype.role.RoleDto;
import team5.prototype.tenant.TenantDto;
import team5.prototype.role.Role;
import team5.prototype.tenant.Tenant;
import team5.prototype.workflow.step.WorkflowStep;
import team5.prototype.workflow.step.WorkflowStepDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;

    public WorkflowDefinitionServiceImpl(WorkflowDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    @Override
    public WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition) {
        // WICHTIG: Setze die Rück-Referenz für jeden Schritt, bevor du speicherst.
        if (definition.getSteps() != null) {
            for (WorkflowStep step : definition.getSteps()) {
                step.setWorkflowDefinition(definition);
            }
        }
        return definitionRepository.save(definition);
    }

    public WorkflowDefinitionDto getWorkflowDefinitionByIdAsDto(Long id) {
        WorkflowDefinition definition = definitionRepository.findById(id).orElse(null);
        return convertToDto(definition);
    }

    public List<WorkflowDefinitionDto> getAllWorkflowDefinitionsAsDto() {
        List<WorkflowDefinition> definitions = definitionRepository.findAll();
        return definitions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public WorkflowDefinitionDto createWorkflowDefinitionAsDto(WorkflowDefinition definition) {
        WorkflowDefinition created = createWorkflowDefinition(definition);
        return convertToDto(created);
    }

    private WorkflowDefinitionDto convertToDto(WorkflowDefinition definition) {
        return WorkflowDefinitionDto.builder()
                .id(definition.getId())
                .name(definition.getName())
                .description(definition.getDescription())
                .tenant(convertTenantToDto(definition.getTenant()))
                .steps(definition.getSteps().stream()
                        .map(this::convertStepToDto)
                        .collect(Collectors.toList()))
                .createdAt(definition.getCreatedAt())
                .updatedAt(definition.getUpdatedAt())
                .build();
    }

    private WorkflowStepDto convertStepToDto(WorkflowStep step) {
        return WorkflowStepDto.builder()
                .id(step.getId())
                .name(step.getName())
                .description(step.getDescription())
                .durationHours(step.getDurationHours())
                .sequenceOrder(step.getSequenceOrder())
                .requiredRole(convertRoleToDto(step.getRequiredRole()))
                .build();
    }

    private RoleDto convertRoleToDto(Role role) {
        if (role == null) return null;
        return RoleDto.builder() // Jetzt funktioniert der Builder
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }

    private TenantDto convertTenantToDto(Tenant tenant) {
        if (tenant == null) return null;
        return TenantDto.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .subdomain(tenant.getSubdomain())
                .active(tenant.getActive())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
