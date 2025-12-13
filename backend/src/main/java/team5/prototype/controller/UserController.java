package team5.prototype.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team5.prototype.dto.TaskStepDto;
import team5.prototype.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TaskDtoMapper mapper;

    @GetMapping("/{userId}/steps")
    public List<TaskStepDto> getActiveSteps(@PathVariable Long userId) {
        return userService.getActiveStepsForUser(userId).stream()
                .map(mapper::toTaskStepDto)
                .toList();
    }
}
