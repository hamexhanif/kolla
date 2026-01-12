package team5.prototype.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import team5.prototype.dto.TaskDetailsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController controller;

    @Test
    void createTaskConvertsToDto() {
        Task task = Task.builder()
                .id(1L)
                .title("t")
                .deadline(LocalDateTime.now())
                .status(TaskStatus.IN_PROGRESS)
                .build();
        when(taskService.createTaskFromDefinition(org.mockito.ArgumentMatchers.any(TaskDto.class)))
                .thenReturn(task);

        TaskDto dto = controller.createTask(new TaskDto());

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS.name());
    }

    @Test
    void getTaskByIdReturnsNotFoundWhenMissing() {
        when(taskService.getTaskByIdAsDto(99L)).thenReturn(Optional.empty());

        ResponseEntity<TaskDto> response = controller.getTaskById(99L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void getAllTasksDelegates() {
        when(taskService.getAllTasksAsDto()).thenReturn(List.of(new TaskDto()));

        List<TaskDto> result = controller.getAllTasks();

        assertThat(result).hasSize(1);
    }

    @Test
    void getProgressAndDetailsReturnOk() {
        TaskProgress progress = new TaskProgress(1L, "t", LocalDateTime.now(), 1, 0, TaskStatus.IN_PROGRESS);
        TaskDetailsDto details = new TaskDetailsDto(1L, "t", null, LocalDateTime.now(), 0, 1, List.of());

        when(taskService.getTaskProgress(1L)).thenReturn(progress);
        when(taskService.getTaskDetails(1L)).thenReturn(details);

        assertThat(controller.getTaskProgress(1L).getBody()).isEqualTo(progress);
        assertThat(controller.getTaskDetails(1L).getBody()).isEqualTo(details);
    }
}
