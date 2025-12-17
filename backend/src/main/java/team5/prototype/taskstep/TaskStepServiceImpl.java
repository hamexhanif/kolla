package team5.prototype.taskstep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.task.TaskService;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

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
