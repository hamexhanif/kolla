package team5.prototype.user;

import org.springframework.web.bind.annotation.*;
import team5.prototype.dto.ManagerDashboardDto;
import team5.prototype.task.TaskService;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    private final TaskService taskService;
    public ManagerController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping("/dashboard")
    public ManagerDashboardDto getManagerDashboard() {
        return taskService.getManagerDashboard();
    }
}