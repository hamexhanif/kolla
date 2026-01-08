package team5.prototype.workflow.definition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkflowDefinitionService definitionService;

    @InjectMocks
    private WorkflowDefinitionController definitionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(definitionController).build();
    }

    @Test
    void getAllDefinitionsReturnsList() throws Exception {
        WorkflowDefinition definition = WorkflowDefinition.builder()
                .id(1L)
                .name("Onboarding")
                .build();

        when(definitionService.getAllDefinitions()).thenReturn(List.of(definition));

        mockMvc.perform(get("/api/workflow-definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Onboarding"));
    }
}
