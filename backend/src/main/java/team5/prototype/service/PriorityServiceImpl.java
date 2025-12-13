package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.Priority;
import team5.prototype.entity.Task;
import team5.prototype.entity.TaskStep;
import team5.prototype.entity.TaskStepStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriorityServiceImpl implements PriorityService {

    private final Clock clock;

    public PriorityServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Priority calculatePriority(Task task) {
        if (task == null || task.getDeadline() == null) {
            return Priority.MEDIUM_TERM;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        long hoursUntilDeadline = Duration.between(now, task.getDeadline()).toHours();
        if (hoursUntilDeadline <= 0) {
            return Priority.IMMEDIATE;
        }

        int remainingHours = calculateRemainingHours(task.getTaskSteps());
        long slackHours = hoursUntilDeadline - remainingHours;

        if (slackHours <= 8) {
            return Priority.IMMEDIATE;
        }
        if (slackHours <= 32) {
            return Priority.MEDIUM_TERM;
        }
        return Priority.LONG_TERM;
    }

    private int calculateRemainingHours(List<TaskStep> steps) {
        if (steps == null) {
            return 0;
        }
        return steps.stream()
                .filter(step -> step.getStatus() != TaskStepStatus.COMPLETED)
                .mapToInt(step -> step.getWorkflowStep().getDurationHours())
                .sum();
    }
}
