package team5.prototype.workflow.definition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.security.TenantProvider;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void getDefinitionByIdReturnsOptional() {
        WorkflowDefinition definition = WorkflowDefinition.builder().id(3L).name("WF").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(definitionRepository.findByIdAndTenant_Id(3L, 1L)).thenReturn(Optional.of(definition));

        Optional<WorkflowDefinition> result = service.getDefinitionById(3L);

        assertThat(result).contains(definition);
    }

    @Test
    void createWorkflowDefinitionSavesEntity() {
        WorkflowDefinition definition = WorkflowDefinition.builder().name("WF").build();
        definition.setSteps(null);
        Tenant tenant = Tenant.builder().id(1L).name("t1").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(definitionRepository.save(definition)).thenReturn(definition);

        WorkflowDefinition saved = service.createWorkflowDefinition(definition);

        assertThat(saved).isEqualTo(definition);
    }

    @Test
    void createWorkflowDefinitionAssignsStepsAndTenant() {
        WorkflowStep step1 = WorkflowStep.builder().name("step-1").sequenceOrder(1).build();
        WorkflowStep step2 = WorkflowStep.builder().name("step-2").sequenceOrder(2).build();
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .name("WF")
                .steps(List.of(step1, step2))
                .build();
        Tenant tenant = Tenant.builder().id(2L).name("t2").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(2L);
        when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenant));
        when(definitionRepository.save(definition)).thenReturn(definition);

        WorkflowDefinition saved = service.createWorkflowDefinition(definition);

        assertThat(saved.getTenant()).isEqualTo(tenant);
        assertThat(step1.getWorkflowDefinition()).isEqualTo(definition);
        assertThat(step2.getWorkflowDefinition()).isEqualTo(definition);
    }

    @Test
    void createWorkflowDefinitionHandlesEmptySteps() {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .name("WF")
                .steps(List.of())
                .build();
        Tenant tenant = Tenant.builder().id(4L).name("t4").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(4L);
        when(tenantRepository.findById(4L)).thenReturn(Optional.of(tenant));
        when(definitionRepository.save(definition)).thenReturn(definition);

        WorkflowDefinition saved = service.createWorkflowDefinition(definition);

        assertThat(saved.getTenant()).isEqualTo(tenant);
    }

    @Test
    void createWorkflowDefinitionThrowsWhenTenantMissing() {
        WorkflowDefinition definition = WorkflowDefinition.builder().name("WF").build();

        when(tenantProvider.getCurrentTenantId()).thenReturn(3L);
        when(tenantRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createWorkflowDefinition(definition))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteWorkflowDefinitionDelegates() {
        WorkflowDefinition definition = WorkflowDefinition.builder().id(11L).build();
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(definitionRepository.findByIdAndTenant_Id(11L, 1L)).thenReturn(Optional.of(definition));

        service.deleteWorkflowDefinition(11L);

        verify(definitionRepository).delete(definition);
    }

    @Test
    void deleteWorkflowDefinitionThrowsWhenMissing() {
        when(tenantProvider.getCurrentTenantId()).thenReturn(1L);
        when(definitionRepository.findByIdAndTenant_Id(11L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteWorkflowDefinition(11L))
                .isInstanceOf(RuntimeException.class);
    }
}
