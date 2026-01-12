package team5.prototype.taskstep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.dto.CompleteStepRequestDto;
import team5.prototype.dto.ManualPriorityRequestDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskStepControllerTest {

    @Mock
    private TaskStepService taskStepService;

    @InjectMocks
    private TaskStepController controller;

    @Test
    void completeTaskStepCallsService() {
        CompleteStepRequestDto request = new CompleteStepRequestDto();
        request.setTaskId(10L);
        request.setUserId(20L);

        ResponseEntity<Void> response = controller.completeTaskStep(30L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(taskStepService).completeTaskStep(10L, 30L, 20L);
    }

    @Test
    void setManualPriorityReturnsDto() {
        TaskStepDto dto = new TaskStepDto();
        dto.setId(1L);
        when(taskStepService.setManualPriorityAndConvertToDto(1L, 2)).thenReturn(dto);

        ResponseEntity<TaskStepDto> response = controller.setManualPriority(1L, new ManualPriorityRequestDto(2));

        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getActorDashboardItemsReturnsItems() {
        ActorDashboardItemDto item = new ActorDashboardItemDto(
                1L, "t", null, null, 2L, "s", 1, TaskStepStatus.ASSIGNED, Priority.IMMEDIATE, null
        );
        when(taskStepService.getActorDashboardItems(5L, null, null, null))
                .thenReturn(List.of(item));

        ResponseEntity<List<ActorDashboardItemDto>> response =
                controller.getActorDashboardItems(5L, null, null, null);

        assertThat(response.getBody()).containsExactly(item);
    }
}
