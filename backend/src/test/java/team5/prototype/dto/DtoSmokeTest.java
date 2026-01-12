package team5.prototype.dto;

import org.junit.jupiter.api.Test;
import team5.prototype.role.RoleDto;
import team5.prototype.task.TaskProgress;
import team5.prototype.task.TaskStatus;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.tenant.TenantDto;
import team5.prototype.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtoSmokeTest {

    @Test
    void canInstantiateDtoAndRecordTypes() {
        ActorDashboardItemDto actor = new ActorDashboardItemDto(
                1L, "t", LocalDateTime.now(), TaskStatus.IN_PROGRESS,
                2L, "s", 1, TaskStepStatus.ASSIGNED, Priority.IMMEDIATE, LocalDateTime.now()
        );
        CompleteStepRequestDto complete = new CompleteStepRequestDto();
        complete.setTaskId(1L);
        complete.setUserId(2L);

        CreateRoleRequestDto createRole = new CreateRoleRequestDto();
        createRole.setName("role");
        createRole.setTenantId(5L);

        CreateTaskRequestDto createTask = new CreateTaskRequestDto(
                1L, "t", "d", LocalDateTime.now(), 2L, null
        );

        CreateUserRequestDto createUser = new CreateUserRequestDto();
        createUser.setUsername("u");
        createUser.setEmail("e");

        ManagerTaskRowDto row = new ManagerTaskRowDto(1L, "t", Priority.LONG_TERM, 1, 2);
        ManagerDashboardDto dashboard = new ManagerDashboardDto(1, 0, 0, List.of(row));

        ManualPriorityRequestDto manual = new ManualPriorityRequestDto(2);

        TaskDetailsStepDto step = new TaskDetailsStepDto(
                1L, "step", TaskStepStatus.ASSIGNED, "a", LocalDateTime.now(), Priority.MEDIUM_TERM
        );
        TaskDetailsDto details = new TaskDetailsDto(
                1L, "t", Priority.MEDIUM_TERM, LocalDateTime.now(), 1, 2, List.of(step)
        );
        TaskProgressDto progressDto = new TaskProgressDto(
                1L, "t", LocalDateTime.now(), 2, 1, TaskStatus.IN_PROGRESS
        );
        TaskStepDto recordStep = new TaskStepDto(
                1L, 2L, "t", 3L, "w", 1, TaskStepStatus.ASSIGNED, Priority.IMMEDIATE, 1, null, null
        );
        TaskResponseDto response = new TaskResponseDto(
                1L, "t", "d", LocalDateTime.now(), TaskStatus.IN_PROGRESS, List.of(recordStep)
        );

        RoleDto roleDto = RoleDto.builder().id(1L).name("r").description("d").build();
        TenantDto tenantDto = TenantDto.builder().id(1L).name("t").subdomain("s").active(true).build();
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("u");

        team5.prototype.taskstep.TaskStepDto taskStepDto = new team5.prototype.taskstep.TaskStepDto();
        taskStepDto.setId(1L);
        taskStepDto.setName("step");

        TaskProgress progress = new TaskProgress(1L, "t", LocalDateTime.now(), 2, 1, TaskStatus.IN_PROGRESS);

        assertThat(actor.taskId()).isEqualTo(1L);
        assertThat(createTask.title()).isEqualTo("t");
        assertThat(dashboard.tasks()).contains(row);
        assertThat(details.steps()).contains(step);
        assertThat(response.steps()).contains(recordStep);
        assertThat(roleDto.getName()).isEqualTo("r");
        assertThat(tenantDto.getName()).isEqualTo("t");
        assertThat(userDto.getUsername()).isEqualTo("u");
        assertThat(taskStepDto.getName()).isEqualTo("step");
        assertThat(progress.totalSteps()).isEqualTo(2);
        assertThat(progressDto.totalSteps()).isEqualTo(2);
        assertThat(manual.manualPriority()).isEqualTo(2);
    }
}
