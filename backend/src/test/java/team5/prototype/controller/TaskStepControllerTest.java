package team5.prototype.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import team5.prototype.entity.*;
import team5.prototype.service.UserService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskStepController.class)
@Import(TaskDtoMapper.class)
class TaskStepControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @Test
    void overridesManualPriority() throws Exception {
        Task task = Task.builder().id(99L).title("Demo Task").build();
        WorkflowStep workflowStep = WorkflowStep.builder().name("Review").sequenceOrder(2).durationHours(2).build();
        TaskStep step = TaskStep.builder()
                .id(55L)
                .task(task)
                .workflowStep(workflowStep)
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.MEDIUM_TERM)
                .manualPriority(2)
                .assignedUser(User.builder().id(100L).build())
                .assignedAt(LocalDateTime.now())
                .build();

        when(userService.overrideManualPriority(eq(55L), eq(2))).thenReturn(step);

        mockMvc.perform(patch("/api/task-steps/55/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"manualPriority":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manualPriority").value(2));

        verify(userService).overrideManualPriority(55L, 2);
    }
}
