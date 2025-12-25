package team5.prototype.dto;

import java.util.List;

public record ManagerDashboardDto(
        long openTasks,
        long overdueTasks,
        long dueTodayTasks,
        List<ManagerTaskRowDto> tasks
) {
}
