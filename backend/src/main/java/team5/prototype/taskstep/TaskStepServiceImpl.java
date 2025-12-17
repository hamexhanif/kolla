package team5.prototype.taskstep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.task.Task;
import team5.prototype.task.TaskRepository;
import team5.prototype.task.TaskStatus;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class TaskStepServiceImpl implements TaskStepService {

    private final TaskStepRepository taskStepRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public TaskStepServiceImpl(TaskStepRepository taskStepRepository, UserRepository userRepository, TaskRepository taskRepository) {
        this.taskStepRepository = taskStepRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public void assignTaskStepToUser(Long taskStepId, Long userId) {
        TaskStep taskStep = findTaskStepById(taskStepId);
        User user = findUserById(userId);
        taskStep.setAssignedUser(user);
        taskStep.setAssignedAt(LocalDateTime.now());
        taskStepRepository.save(taskStep);
    }

    @Override
    @Transactional
    public void completeTaskStep(Long taskStepId, Long userId) {
        TaskStep step = findTaskStepById(taskStepId);

        if (!Objects.equals(step.getAssignedUser().getId(), userId)) {
            throw new IllegalArgumentException("Benutzer ist nicht dem Arbeitsschritt zugeordnet");
        }
        if (step.getStatus() == TaskStepStatus.COMPLETED) {
            return;
        }

        step.setStatus(TaskStepStatus.COMPLETED);
        step.setCompletedAt(LocalDateTime.now());
        taskStepRepository.save(step);

        moveToNextStep(step.getTask());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskStep> getTaskStepsByUserId(Long userId) {
        return taskStepRepository.findAllByAssignedUserId(userId);
    }

    @Override
    @Transactional
    public void overridePriority(Long taskStepId, Priority priority) {
        TaskStep step = findTaskStepById(taskStepId);
        step.setPriority(priority);
        taskStepRepository.save(step);
    }

    @Override
    public void calculatePriority(Long taskStepId) {
        // TODO: Logik zur Neuberechnung der Priorität implementieren.
        // Dies würde wahrscheinlich den PriorityService aufrufen.
        System.out.println("Priorität für TaskStep " + taskStepId + " wird neu berechnet.");
    }

    // --- Private Hilfsmethoden ---

    private void moveToNextStep(Task task) {
        // ... (Ihre existierende moveToNextStep-Logik hier) ...
    }

    private TaskStep findTaskStepById(Long taskStepId) {
        return taskStepRepository.findById(taskStepId)
                .orElseThrow(() -> new EntityNotFoundException("TaskStep %d nicht gefunden".formatted(taskStepId)));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User %d nicht gefunden".formatted(userId)));
    }
}