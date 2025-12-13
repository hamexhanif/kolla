package team5.prototype.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.entity.Priority;
import team5.prototype.entity.TaskStep;
import team5.prototype.entity.TaskStepStatus;
import team5.prototype.repository.TaskStepRepository;
import team5.prototype.repository.UserRepository;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final Map<Priority, Integer> PRIORITY_RANKING = buildPriorityRanking();

    private final UserRepository userRepository;
    private final TaskStepRepository taskStepRepository;

    public UserServiceImpl(UserRepository userRepository,
                           TaskStepRepository taskStepRepository) {
        this.userRepository = userRepository;
        this.taskStepRepository = taskStepRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStep> getActiveStepsForUser(Long userId) {
        assertUserExists(userId);
        List<TaskStep> steps = taskStepRepository.findByAssignedUserIdAndStatusNot(userId, TaskStepStatus.COMPLETED);
        return steps.stream()
                .filter(step -> step.getStatus() != TaskStepStatus.WAITING)
                .sorted(stepComparator())
                .toList();
    }

    @Override
    @Transactional
    public TaskStep overrideManualPriority(Long taskStepId, Integer manualPriority) {
        TaskStep step = taskStepRepository.findById(taskStepId)
                .orElseThrow(() -> new EntityNotFoundException("TaskStep %d nicht gefunden".formatted(taskStepId)));
        step.setManualPriority(manualPriority);
        return taskStepRepository.save(step);
    }

    private Comparator<TaskStep> stepComparator() {
        return Comparator
                .comparingInt(UserServiceImpl::manualPriorityValue)
                .thenComparing(step -> PRIORITY_RANKING.getOrDefault(step.getPriority(), Integer.MAX_VALUE))
                .thenComparing(TaskStep::getAssignedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(step -> step.getWorkflowStep().getSequenceOrder());
    }

    private static int manualPriorityValue(TaskStep step) {
        Integer manual = step.getManualPriority();
        return manual == null ? Integer.MAX_VALUE : manual;
    }

    private void assertUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId darf nicht null sein.");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User %d nicht gefunden".formatted(userId)));
    }

    private static Map<Priority, Integer> buildPriorityRanking() {
        Map<Priority, Integer> ranking = new EnumMap<>(Priority.class);
        ranking.put(Priority.IMMEDIATE, 0);
        ranking.put(Priority.MEDIUM_TERM, 1);
        ranking.put(Priority.LONG_TERM, 2);
        return ranking;
    }
}
