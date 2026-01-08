package team5.prototype.workflow.definition;

import org.springframework.stereotype.Service;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;

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

    @Override // Diese Annotation ist wichtig
    public List<WorkflowDefinition> getAllDefinitions() {
        return definitionRepository.findAllByTenant_Id(tenantProvider.getCurrentTenantId());
    }

    // --- NEUE IMPLEMENTIERUNGEN ---
    @Override
    public WorkflowDefinition createWorkflowDefinition(WorkflowDefinition definition) {
        // Hier könnten Validierungen hinzukommen
        Long tenantId = tenantProvider.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant mit ID " + tenantId + " nicht gefunden!"));
        definition.setTenant(tenant);
        return definitionRepository.save(definition);
    }

    @Override
    public void deleteWorkflowDefinition(Long definitionId) {
        WorkflowDefinition definition = definitionRepository.findByIdAndTenant_Id(definitionId,
                        tenantProvider.getCurrentTenantId())
                .orElseThrow(() -> new RuntimeException("WorkflowDefinition mit ID " + definitionId + " nicht gefunden!"));
        definitionRepository.delete(definition);
    }
}

