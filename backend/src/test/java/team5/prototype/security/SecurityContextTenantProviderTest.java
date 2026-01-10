package team5.prototype.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityContextTenantProviderTest {

    private final SecurityContextTenantProvider provider = new SecurityContextTenantProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentTenantIdThrowsWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(provider::getCurrentTenantId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCurrentTenantIdThrowsWhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(provider::getCurrentTenantId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCurrentTenantIdThrowsWhenPrincipalUnknown() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("user");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(provider::getCurrentTenantId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCurrentTenantIdThrowsWhenTenantMissing() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal("user", null));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(provider::getCurrentTenantId)
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getCurrentTenantIdReturnsTenant() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new UserPrincipal("user", 4L));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(provider.getCurrentTenantId()).isEqualTo(4L);
    }
}
