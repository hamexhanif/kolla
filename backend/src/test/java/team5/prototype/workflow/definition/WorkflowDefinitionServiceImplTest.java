package team5.prototype.workflow.definition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.role.Role;
import team5.prototype.tenant.Tenant;
import team5.prototype.workflow.step.WorkflowStep;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionServiceImplTest {

    @Mock
    private WorkflowDefinitionRepository definitionRepository;

    @InjectMocks
    private WorkflowDefinitionServiceImpl service;

    @Test
    void createWorkflowDefinitionSetsBackReferenceOnSteps() {
        WorkflowStep step = WorkflowStep.builder().id(1L).name("S").sequenceOrder(1).build();
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .id(2L)
                .name("WF")
                .steps(List.of(step))
                .build();

        when(definitionRepository.save(any(WorkflowDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowDefinition created = service.createWorkflowDefinition(definition);

        assertThat(created.getSteps().get(0).getWorkflowDefinition()).isEqualTo(created);
    }

    @Test
    void getWorkflowDefinitionByIdAsDtoMapsFields() {
        Tenant tenant = Tenant.builder().id(1L).name("t").subdomain("s").active(true).build();
        Role role = Role.builder().id(2L).name("r").description("d").build();
        WorkflowStep step = WorkflowStep.builder()
                .id(3L)
                .name("Step")
                .sequenceOrder(1)
                .durationHours(2)
                .requiredRole(role)
                .build();
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .id(4L)
                .name("WF")
                .description("desc")
                .tenant(tenant)
                .steps(List.of(step))
                .build();

        when(definitionRepository.findById(4L)).thenReturn(Optional.of(definition));

        WorkflowDefinitionDto dto = service.getWorkflowDefinitionByIdAsDto(4L);

        assertThat(dto.getName()).isEqualTo("WF");
        assertThat(dto.getTenant().getName()).isEqualTo("t");
        assertThat(dto.getSteps()).hasSize(1);
        assertThat(dto.getSteps().get(0).getRequiredRole().getName()).isEqualTo("r");
    }
}
