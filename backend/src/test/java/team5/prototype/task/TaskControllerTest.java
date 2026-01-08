package team5.prototype.task;

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
import team5.prototype.dto.TaskDetailsDto;
import team5.prototype.dto.TaskDetailsStepDto;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStepStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void createTaskReturnsDto() throws Exception {
        TaskDto request = new TaskDto();
        request.setTitle("New Task");
        request.setDescription("Desc");
        request.setDeadline(LocalDateTime.of(2026, 1, 5, 10, 0));
        request.setWorkflowDefinitionId(3L);
        request.setCreatorUserId(5L);
        request.setStepAssignments(Map.of(10L, 20L));

        Task created = Task.builder()
                .id(99L)
                .title("New Task")
                .description("Desc")
                .deadline(request.getDeadline())
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskService.createTaskFromDefinition(any(TaskDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.deadline").value("2026-01-05T10:00:00"));
    }

    @Test
    void getAllTasksReturnsDtos() throws Exception {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        dto.setTitle("Task One");
        dto.setStatus("NOT_STARTED");

        when(taskService.getAllTasksAsDto()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task One"))
                .andExpect(jsonPath("$[0].status").value("NOT_STARTED"));
    }

    @Test
    void getTaskByIdReturnsNotFoundWhenMissing() throws Exception {
        when(taskService.getTaskByIdAsDto(44L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/44"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTaskProgressReturnsSnapshot() throws Exception {
        TaskProgress progress = new TaskProgress(
                77L,
                "Progress Task",
                LocalDateTime.of(2026, 2, 1, 12, 0),
                5,
                2,
                TaskStatus.IN_PROGRESS
        );

        when(taskService.getTaskProgress(77L)).thenReturn(progress);

        mockMvc.perform(get("/api/tasks/77/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(77))
                .andExpect(jsonPath("$.totalSteps").value(5))
                .andExpect(jsonPath("$.completedSteps").value(2))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getTaskDetailsReturnsDto() throws Exception {
        TaskDetailsStepDto step = new TaskDetailsStepDto(
                11L,
                "Review",
                TaskStepStatus.ASSIGNED,
                "Alex Doe",
                LocalDateTime.of(2026, 2, 2, 9, 0),
                Priority.MEDIUM_TERM
        );
        TaskDetailsDto details = new TaskDetailsDto(
                88L,
                "Detail Task",
                Priority.MEDIUM_TERM,
                LocalDateTime.of(2026, 2, 5, 10, 0),
                1,
                3,
                List.of(step)
        );

        when(taskService.getTaskDetails(88L)).thenReturn(details);

        mockMvc.perform(get("/api/tasks/88/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(88))
                .andExpect(jsonPath("$.steps[0].stepName").value("Review"));
    }

    @Test
    void getManagerDashboardReturnsBadRequestWhenEndpointMissing() throws Exception {
        mockMvc.perform(get("/api/tasks/manager-dashboard"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(taskService);
    }
}
