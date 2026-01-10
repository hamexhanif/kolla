package team5.prototype.workflow.definition;

import org.springframework.stereotype.Service;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.util.List;
import java.util.Optional;

@Service
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {

    private final WorkflowDefinitionRepository definitionRepository;
    private final TenantRepository tenantRepository;
    private final TenantProvider tenantProvider;

    public WorkflowDefinitionServiceImpl(WorkflowDefinitionRepository definitionRepository,
                                         TenantRepository tenantRepository,
                                         TenantProvider tenantProvider) {
        this.definitionRepository = definitionRepository;
        this.tenantRepository = tenantRepository;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition) {
        if (definition.getSteps() != null) {
            for (WorkflowStep step : definition.getSteps()) {
                step.setWorkflowDefinition(definition);
            }
        }

        Long tenantId = tenantProvider.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!"));
        definition.setTenant(tenant);
        return definitionRepository.save(definition);
    }

    @Override
    public List<WorkflowDefinition> getAllDefinitions() {
        return definitionRepository.findAllByTenant_Id(tenantProvider.getCurrentTenantId());
    }

    @Override
    public Optional<WorkflowDefinition> getDefinitionById(Long id) {
        return definitionRepository.findByIdAndTenant_Id(id, tenantProvider.getCurrentTenantId());
    }

    @Override
    public void deleteWorkflowDefinition(Long id) {
        WorkflowDefinition definition = definitionRepository.findByIdAndTenant_Id(id, tenantProvider.getCurrentTenantId())
                .orElseThrow(() -> new RuntimeException("WorkflowDefinition %d nicht gefunden".formatted(id)));
        definitionRepository.delete(definition);
    }
}
