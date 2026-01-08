package team5.prototype.user;

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
import team5.prototype.dto.CreateUserRequestDto;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getAllUsersReturnsDtos() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("alex")
                .email("alex@example.com")
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("alex"))
                .andExpect(jsonPath("$[0].email").value("alex@example.com"));
    }

    @Test
    void getUserByIdReturnsNotFoundWhenMissing() throws Exception {
        when(userService.getUserById(5L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserReturnsDto() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto();
        request.setUsername("alex");
        request.setEmail("alex@example.com");
        request.setPassword("secret");
        request.setTenantId(1L);
        User created = User.builder()
                .id(9L)
                .username("alex")
                .email("alex@example.com")
                .build();

        when(userService.createUser(any(CreateUserRequestDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.username").value("alex"));
    }

    @Test
    void updateUserReturnsDto() throws Exception {
        User request = User.builder()
                .username("maria")
                .email("maria@example.com")
                .build();
        User updated = User.builder()
                .id(12L)
                .username("maria")
                .email("maria@example.com")
                .build();

        when(userService.updateUser(any(Long.class), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.email").value("maria@example.com"));
    }

    @Test
    void deleteUserReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(20L);

        mockMvc.perform(delete("/api/users/20"))
                .andExpect(status().isNoContent());
    }
}
