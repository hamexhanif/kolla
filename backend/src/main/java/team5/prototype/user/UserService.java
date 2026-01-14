package team5.prototype.user;

import java.util.List;
import java.util.Optional;
import team5.prototype.dto.CreateUserRequestDto;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long userId);
    User updateUser(Long userId, UpdateUserRequestDto requestDto);
    void deleteUser(Long userId);
    User createUser(CreateUserRequestDto requestDto);
}
