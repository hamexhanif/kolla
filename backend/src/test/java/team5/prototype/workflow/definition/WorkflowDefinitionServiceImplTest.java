package team5.prototype.workflow.definition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionServiceImplTest {

    @Mock
    private WorkflowDefinitionRepository definitionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantProvider tenantProvider;

    @InjectMocks
    private WorkflowDefinitionServiceImpl service;

    @Test
    void getAllDefinitionsReturnsList() {
        WorkflowDefinition definition = WorkflowDefinition.builder().name("WF").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(definitionRepository.findAllByTenant_Id(1L)).thenReturn(List.of(definition));

        List<WorkflowDefinition> result = service.getAllDefinitions();

        assertThat(result).containsExactly(definition);
    }

    @Test
    void createWorkflowDefinitionSavesEntity() {
        WorkflowDefinition definition = WorkflowDefinition.builder().name("WF").build();
        Tenant tenant = Tenant.builder().id(1L).name("t1").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(definitionRepository.save(definition)).thenReturn(definition);

        WorkflowDefinition saved = service.createWorkflowDefinition(definition);

        assertThat(saved).isEqualTo(definition);
    }

    @Test
    void deleteWorkflowDefinitionDelegates() {
        WorkflowDefinition definition = WorkflowDefinition.builder().id(11L).build();
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(definitionRepository.findByIdAndTenant_Id(11L, 1L)).thenReturn(Optional.of(definition));

        service.deleteWorkflowDefinition(11L);

        verify(definitionRepository).delete(definition);
    }
}
