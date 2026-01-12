package team5.prototype.user;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team5.prototype.dto.CreateUserRequestDto;
import team5.prototype.role.Role;
import team5.prototype.role.RoleRepository;
import team5.prototype.tenant.Tenant;
import team5.prototype.tenant.TenantRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserEncodesPasswordAndAddsRole() {
        Tenant tenant = Tenant.builder().id(1L).build();
        Role role = Role.builder().id(2L).name("role").build();
        CreateUserRequestDto dto = new CreateUserRequestDto();
        dto.setTenantId(tenant.getId());
        dto.setRoleId(role.getId());
        dto.setUsername("u");
        dto.setEmail("e");
        dto.setPassword("p");

        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("p")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(dto);

        assertThat(created.getPasswordHash()).isEqualTo("hashed");
        assertThat(created.getRoles()).contains(role);
    }

    @Test
    void updateUserReplacesRoles() {
        Role role = Role.builder().id(5L).build();
        User user = User.builder().id(10L).roles(new java.util.HashSet<>()).build();
        UpdateUserRequestDto dto = new UpdateUserRequestDto();
        dto.setUsername("new");
        dto.setEmail("new@e");
        dto.setRoleIds(List.of(role.getId()));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(List.of(role.getId()))).thenReturn(List.of(role));

        User updated = userService.updateUser(10L, dto);

        assertThat(updated.getRoles()).containsExactly(role);
    }

    @Test
    void deleteUserMarksInactive() {
        User user = User.builder().id(11L).active(true).build();
        when(userRepository.findById(11L)).thenReturn(Optional.of(user));

        userService.deleteUser(11L);

        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new UpdateUserRequestDto()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
