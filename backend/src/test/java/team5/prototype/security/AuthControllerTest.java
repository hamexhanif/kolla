package team5.prototype.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void loginReturnsUnauthorizedOnNull() {
        AuthDto request = new AuthDto("token", 1L);
        request.setEmail("a@b.com");
        request.setPassword("p");

        when(authService.login("a@b.com", "p")).thenReturn(null);

        ResponseEntity<AuthDto> response = controller.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void loginReturnsOkOnSuccess() {
        AuthDto request = new AuthDto("token", 1L);
        request.setEmail("a@b.com");
        request.setPassword("p");
        AuthDto responseDto = new AuthDto("newToken", 2L);

        when(authService.login("a@b.com", "p")).thenReturn(responseDto);

        ResponseEntity<AuthDto> response = controller.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseDto);
    }
}
