package team5.prototype.init;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team5.prototype.role.Role;
import team5.prototype.role.RoleRepository;
import team5.prototype.task.TaskDto;
import team5.prototype.task.TaskService;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkflowDefinitionRepository workflowDefinitionRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TaskService taskService;

    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        dataSeeder = new DataSeeder(
                roleRepository,
                userRepository,
                workflowDefinitionRepository,
                tenantRepository,
                passwordEncoder,
                taskService
        );
    }

    @Test
    void runSkipsWhenDataExists() throws Exception {
        when(tenantRepository.count()).thenReturn(1L);

        dataSeeder.run();

        verify(tenantRepository).count();
        verifyNoInteractions(roleRepository, userRepository, workflowDefinitionRepository, taskService);
    }

    @Test
    void runSeedsWhenDatabaseEmpty() throws Exception {
        when(tenantRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash");
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setId(1L);
            return tenant;
        });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(5L);
            }
            return user;
        });
        when(userRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowDefinitionRepository.save(any(WorkflowDefinition.class))).thenAnswer(invocation -> {
            WorkflowDefinition wf = invocation.getArgument(0);
            wf.setId(10L);
            return wf;
        });

        dataSeeder.run();

        verify(tenantRepository).save(any(Tenant.class));
        verify(roleRepository).saveAll(anyList());
        verify(userRepository).saveAll(anyList());
        verify(workflowDefinitionRepository).save(any(WorkflowDefinition.class));

        ArgumentCaptor<TaskDto> taskCaptor = ArgumentCaptor.forClass(TaskDto.class);
        verify(taskService).createTaskFromDefinition(taskCaptor.capture());
        TaskDto dto = taskCaptor.getValue();
        assertThat(dto.getWorkflowDefinitionId()).isEqualTo(10L);
        assertThat(dto.getTitle()).isEqualTo("Erstes Feature implementieren");
        assertThat(dto.getCreatorUserId()).isEqualTo(5L);
    }
}
