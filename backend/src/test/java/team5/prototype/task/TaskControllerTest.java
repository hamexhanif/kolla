package team5.prototype.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController controller;

    @Test
    void getTaskByIdReturnsNotFoundWhenMissing() {
        when(taskService.getTaskByIdAsDto(1L)).thenReturn(Optional.empty());

        ResponseEntity<TaskDto> response = controller.getTaskById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getTaskByIdReturnsOkWhenFound() {
        TaskDto dto = new TaskDto();
        when(taskService.getTaskByIdAsDto(1L)).thenReturn(Optional.of(dto));

        ResponseEntity<TaskDto> response = controller.getTaskById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void createTaskOmitsStatusWhenNull() {
        Task task = Task.builder()
                .id(7L)
                .title("t")
                .deadline(LocalDateTime.now())
                .status(null)
                .build();
        when(taskService.createTaskFromDefinition(any(TaskDto.class))).thenReturn(task);

        TaskDto response = controller.createTask(new TaskDto());

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getStatus()).isNull();
    }

    @Test
    void createTaskCopiesStatusWhenPresent() {
        Task task = Task.builder()
                .id(8L)
                .title("t")
                .status(TaskStatus.IN_PROGRESS)
                .build();
        when(taskService.createTaskFromDefinition(any(TaskDto.class))).thenReturn(task);

        TaskDto response = controller.createTask(new TaskDto());

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS.name());
    }
}
