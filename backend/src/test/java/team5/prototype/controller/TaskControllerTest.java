package team5.prototype.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import team5.prototype.dto.CreateTaskRequestDto;
import team5.prototype.entity.*;
import team5.prototype.service.TaskCreationRequest;
import team5.prototype.service.TaskProgress;
import team5.prototype.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(TaskDtoMapper.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TaskService taskService;

    @Test
    void createTaskReturnsPersistedEntity() throws Exception {
        Task task = sampleTask();
        when(taskService.createTaskFromDefinition(any(TaskCreationRequest.class))).thenReturn(task);

        CreateTaskRequestDto requestDto = new CreateTaskRequestDto(
                11L,
                "Build Feature",
                "Some description",
                LocalDateTime.now().plusDays(1),
                42L,
                Map.of()
        );

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.steps[0].workflowStepName").value("Code"));
    }

    @Test
    void completeStepDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/tasks/1/steps/2/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":5}
                                """))
                .andExpect(status().isNoContent());

        verify(taskService).completeStep(1L, 2L, 5L);
    }

    @Test
    void exposesTaskProgress() throws Exception {
        TaskProgress progress = new TaskProgress(9L, "Build", LocalDateTime.now().plusDays(2), 3, 1, TaskStatus.IN_PROGRESS);
        when(taskService.getTaskProgress(9L)).thenReturn(progress);

        mockMvc.perform(get("/api/tasks/9/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(9))
                .andExpect(jsonPath("$.totalSteps").value(3));
    }

    private Task sampleTask() {
        Task task = Task.builder()
                .id(77L)
                .title("Build Feature")
                .description("Some description")
                .deadline(LocalDateTime.now().plusDays(2))
                .status(TaskStatus.NOT_STARTED)
                .build();

        WorkflowStep workflowStep = WorkflowStep.builder()
                .id(1L)
                .name("Code")
                .sequenceOrder(0)
                .durationHours(4)
                .build();

        TaskStep step = TaskStep.builder()
                .id(700L)
                .task(task)
                .workflowStep(workflowStep)
                .assignedUser(User.builder().id(5L).build())
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .build();

        task.setTaskSteps(List.of(step));
        return task;
    }
}
