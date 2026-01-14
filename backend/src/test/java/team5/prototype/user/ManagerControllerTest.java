package team5.prototype.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team5.prototype.dto.ManagerDashboardDto;
import team5.prototype.task.TaskService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ManagerController controller;

    @Test
    void getManagerDashboardDelegates() {
        ManagerDashboardDto dto = new ManagerDashboardDto(1, 2, 3, List.of());
        when(taskService.getManagerDashboard()).thenReturn(dto);

        ManagerDashboardDto result = controller.getManagerDashboard();

        assertThat(result).isEqualTo(dto);
    }
}
