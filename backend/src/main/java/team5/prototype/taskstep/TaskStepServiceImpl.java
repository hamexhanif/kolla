package team5.prototype.taskstep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.task.TaskService;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class TaskStepServiceImpl implements TaskStepService {

    private final TaskStepRepository taskStepRepository;
    private final UserRepository userRepository;
    private final TaskService taskService;

    public TaskStepServiceImpl(TaskStepRepository taskStepRepository,
                               UserRepository userRepository,
                               TaskService taskService) {
        this.taskStepRepository = taskStepRepository;
        this.userRepository = userRepository;
        this.taskService = taskService;
    }

    @Override
    @Transactional
    public TaskStep assignTaskStepToUser(Long taskStepId, Long userId) {
        TaskStep step = findTaskStep(taskStepId);
        User user = findUser(userId);
        step.setAssignedUser(user);
        step.setStatus(TaskStepStatus.ASSIGNED);
        if (step.getAssignedAt() == null) {
            step.setAssignedAt(LocalDateTime.now());
        }
        return taskStepRepository.save(step);
    }

    @Override
    @Transactional
    public TaskStep setManualPriority(Long taskStepId, int manualPriority) {
        TaskStep step = findTaskStep(taskStepId);
        step.setManualPriority(manualPriority);
        step.setPriority(mapManualPriority(manualPriority));
        return taskStepRepository.save(step);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStep> getActiveTaskStepsByUser(Long userId) {
        return taskStepRepository.findByAssignedUserIdAndStatusNot(userId, TaskStepStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActorDashboardItemDto> getActorDashboardItems(Long userId,
                                                              TaskStepStatus status,
                                                              Priority priority,
                                                              String query) {
        Stream<TaskStep> steps = taskStepRepository
                .findByAssignedUserIdAndStatusNot(userId, TaskStepStatus.COMPLETED)
                .stream();
        if (status != null) {
            steps = steps.filter(step -> step.getStatus() == status);
        }
        if (priority != null) {
            steps = steps.filter(step -> step.getPriority() == priority);
        }
        if (query != null && !query.isBlank()) {
            String needle = query.toLowerCase(Locale.ROOT);
            steps = steps.filter(step -> containsIgnoreCase(step.getTask().getTitle(), needle)
                    || containsIgnoreCase(step.getWorkflowStep().getName(), needle));
        }
        return steps.map(this::toActorDashboardItem).toList();
    }

    @Override
    @Transactional
    public void completeTaskStep(Long taskId, Long taskStepId, Long userId) {
        taskService.completeStep(taskId, taskStepId, userId);
    }

    private TaskStep findTaskStep(Long id) {
        return taskStepRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TaskStep %d nicht gefunden".formatted(id)));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User %d nicht gefunden".formatted(id)));
    }

    private ActorDashboardItemDto toActorDashboardItem(TaskStep step) {
        return new ActorDashboardItemDto(
                step.getTask().getId(),
                step.getTask().getTitle(),
                step.getTask().getDeadline(),
                step.getTask().getStatus(),
                step.getId(),
                step.getWorkflowStep().getName(),
                step.getWorkflowStep().getSequenceOrder(),
                step.getStatus(),
                step.getPriority(),
                step.getAssignedAt()
        );
    }

    private boolean containsIgnoreCase(String value, String needleLower) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(needleLower);
    }

    private Priority mapManualPriority(int manualPriority) {
        if (manualPriority <= 1) {
            return Priority.IMMEDIATE;
        }
        if (manualPriority == 2) {
            return Priority.MEDIUM_TERM;
        }
        return Priority.LONG_TERM;
    }
}
