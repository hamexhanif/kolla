package team5.prototype.taskstep;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.notification.NotificationService;
import team5.prototype.task.TaskService;
import team5.prototype.tenant.TenantContext;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskStepServiceImplTenantIsolationTest {

    @Mock
    private TaskStepRepository taskStepRepository;
    @Mock
    private TaskService taskService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskStepServiceImpl taskStepService;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllTaskStepsByUserIdUsesTenantContext() {
        Long tenantId = 5L;
        TenantContext.setTenantId(tenantId);
        when(taskStepRepository.findAllByAssignedUserIdAndTask_Tenant_Id(10L, tenantId))
                .thenReturn(List.of());

        taskStepService.getAllTaskStepsByUserId(10L);

        verify(taskStepRepository).findAllByAssignedUserIdAndTask_Tenant_Id(10L, tenantId);
    }
}
