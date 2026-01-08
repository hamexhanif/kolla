package team5.prototype.taskstep;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.dto.CompleteStepRequestDto;
import team5.prototype.dto.ManualPriorityRequestDto;
import team5.prototype.task.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskStepControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private TaskStepService taskStepService;

    @InjectMocks
    private TaskStepController taskStepController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskStepController).build();
    }

    @Test
    void completeTaskStepDelegatesToService() throws Exception {
        CompleteStepRequestDto request = new CompleteStepRequestDto();
        request.setTaskId(99L);
        request.setUserId(10L);

        doNothing().when(taskStepService).completeTaskStep(99L, 5L, 10L);

        mockMvc.perform(post("/api/task-steps/5/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(taskStepService).completeTaskStep(99L, 5L, 10L);
    }

    @Test
    void setManualPriorityReturnsDto() throws Exception {
        TaskStepDto response = new TaskStepDto();
        response.setId(3L);
        response.setName("Review");
        response.setStatus(TaskStepStatus.ASSIGNED.name());
        response.setAssignedUsername("alex");
        response.setPriority(Priority.MEDIUM_TERM.name());

        when(taskStepService.setManualPriorityAndConvertToDto(3L, 2)).thenReturn(response);

        ManualPriorityRequestDto request = new ManualPriorityRequestDto(2);

        mockMvc.perform(post("/api/task-steps/3/set-priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.priority").value("MEDIUM_TERM"));
    }

    @Test
    void getActorDashboardItemsReturnsList() throws Exception {
        ActorDashboardItemDto item = new ActorDashboardItemDto(
                1L,
                "Task One",
                LocalDateTime.of(2026, 2, 10, 9, 0),
                TaskStatus.NOT_STARTED,
                7L,
                "Review",
                1,
                TaskStepStatus.ASSIGNED,
                Priority.MEDIUM_TERM,
                LocalDateTime.of(2026, 2, 1, 9, 0)
        );

        when(taskStepService.getActorDashboardItems(10L, TaskStepStatus.ASSIGNED, Priority.MEDIUM_TERM, "review"))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/task-steps/actor-dashboard")
                        .param("userId", "10")
                        .param("status", "ASSIGNED")
                        .param("priority", "MEDIUM_TERM")
                        .param("query", "review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(1))
                .andExpect(jsonPath("$[0].stepName").value("Review"));
    }
}
