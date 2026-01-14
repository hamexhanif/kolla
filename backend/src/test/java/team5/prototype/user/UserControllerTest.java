package team5.prototype.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepService;
import team5.prototype.taskstep.TaskStepStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private TaskStepService taskStepService;

    @InjectMocks
    private UserController controller;

    @Test
    void getAllUsersMapsTaskCounts() {
        User user = User.builder().id(1L).username("u").email("e").build();
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(taskStepService.getAllTaskStepsByUserId(1L)).thenReturn(List.of(
                TaskStep.builder().status(TaskStepStatus.WAITING).build(),
                TaskStep.builder().status(TaskStepStatus.COMPLETED).build(),
                TaskStep.builder().status(TaskStepStatus.ASSIGNED).build()
        ));

        List<UserDto> result = controller.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedTasks()).isEqualTo(1);
        assertThat(result.get(0).getCompletedTasks()).isEqualTo(1);
        assertThat(result.get(0).getInProgressTasks()).isEqualTo(1);
    }

    @Test
    void getUserByIdReturnsNotFoundWhenMissing() {
        when(userService.getUserById(5L)).thenReturn(Optional.empty());

        ResponseEntity<UserDto> response = controller.getUserById(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void createUserMapsDto() {
        User user = User.builder().id(2L).username("u").email("e").build();
        when(userService.createUser(org.mockito.ArgumentMatchers.any(CreateUserRequestDto.class)))
                .thenReturn(user);
        when(taskStepService.getAllTaskStepsByUserId(2L)).thenReturn(List.of());

        UserDto dto = controller.createUser(new CreateUserRequestDto());

        assertThat(dto.getId()).isEqualTo(2L);
    }

    @Test
    void deleteUserDelegates() {
        ResponseEntity<Void> response = controller.deleteUser(9L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(userService).deleteUser(9L);
    }

    @Test
    void getMyTasksDelegates() {
        ActorDashboardItemDto item = new ActorDashboardItemDto(
                1L, "t", null, null, 2L, "s", 1, TaskStepStatus.ASSIGNED, Priority.IMMEDIATE, null
        );
        when(taskStepService.getActorDashboardItems(3L)).thenReturn(List.of(item));

        List<ActorDashboardItemDto> result = controller.getMyTasks(3L);

        assertThat(result).containsExactly(item);
    }
}
