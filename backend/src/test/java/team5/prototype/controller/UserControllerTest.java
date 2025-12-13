package team5.prototype.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import team5.prototype.entity.*;
import team5.prototype.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TaskDtoMapper.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    void listsActiveStepsForUser() throws Exception {
        Task task = Task.builder().id(1L).title("Build Feature").build();
        WorkflowStep workflowStep = WorkflowStep.builder().name("Code").sequenceOrder(0).durationHours(4).build();
        TaskStep step = TaskStep.builder()
                .id(10L)
                .task(task)
                .workflowStep(workflowStep)
                .assignedUser(User.builder().id(5L).build())
                .status(TaskStepStatus.ASSIGNED)
                .priority(Priority.IMMEDIATE)
                .manualPriority(1)
                .assignedAt(LocalDateTime.now())
                .build();
        when(userService.getActiveStepsForUser(5L)).thenReturn(List.of(step));

        mockMvc.perform(get("/api/users/5/steps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].priority").value("IMMEDIATE"))
                .andExpect(jsonPath("$[0].workflowStepName").value("Code"));
    }
}
