package team5.prototype.taskstep;

import org.springframework.stereotype.Service;
import team5.prototype.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriorityServiceImpl implements PriorityService {

    @Override
    public Priority calculatePriority(TaskStep taskStep) {
        Task task = taskStep.getTask();
        LocalDateTime now = LocalDateTime.now();
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
