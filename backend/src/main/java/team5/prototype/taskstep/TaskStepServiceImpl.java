package team5.prototype.taskstep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.notification.NotificationService;
import team5.prototype.task.Task;
import team5.prototype.task.TaskRepository;
import team5.prototype.task.TaskStatus;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TaskStepServiceImpl implements TaskStepService {

    private final TaskStepRepository taskStepRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    public TaskStepServiceImpl(TaskStepRepository taskStepRepository,
                               UserRepository userRepository,
                               TaskRepository taskRepository,
                               NotificationService notificationService) {
        this.taskStepRepository = taskStepRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void assignTaskStepToUser(Long taskStepId, Long userId) {
        TaskStep step = findTaskStepById(taskStepId);
        User user = findUserById(userId);
        step.setAssignedUser(user);
        step.setAssignedAt(LocalDateTime.now());
        taskStepRepository.save(step);
    }

    @Override
    @Transactional
    public void completeTaskStep(Long taskStepId, Long userId) {
        // 1. Lade den spezifischen TaskStep, der abgeschlossen werden soll
        TaskStep step = taskStepRepository.findById(taskStepId)
                .orElseThrow(() -> new EntityNotFoundException("TaskStep mit ID " + taskStepId + " nicht gefunden."));

        // 2. Sicherheits-Checks
        if (step.getAssignedUser() == null || !Objects.equals(step.getAssignedUser().getId(), userId)) {
            throw new IllegalArgumentException("Benutzer ist nicht dem Arbeitsschritt zugeordnet.");
        }
        if (step.getStatus() == TaskStepStatus.COMPLETED) {
            return; // Nichts zu tun, da bereits erledigt
        }

        // 3. Status des aktuellen Schritts aktualisieren
        step.setStatus(TaskStepStatus.COMPLETED);
        step.setCompletedAt(LocalDateTime.now());
        taskStepRepository.save(step);

        moveToNextStep(step.getTask());

        Map<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("taskId", step.getTask().getId());
        updateInfo.put("message", "Ein Schritt wurde abgeschlossen!");
        notificationService.sendProgressUpdate(updateInfo);
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
        System.out.println("TODO: Priorität für Step " + taskStepId + " berechnen.");
    }


    // --- Private Hilfsmethoden ---

    private void moveToNextStep(Task task) {
        List<TaskStep> steps = task.getTaskSteps();
        int currentIndex = task.getCurrentStepIndex();
        int nextIndex = currentIndex + 1;

        if (nextIndex >= steps.size()) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
        } else {
            TaskStep nextStep = steps.get(nextIndex);

            // TODO: Logik zur Zuweisung des nächsten Benutzers (resolveAssignee)
            // nextStep.setAssignedUser(...);
            nextStep.setStatus(TaskStepStatus.ASSIGNED);
            nextStep.setAssignedAt(LocalDateTime.now());
            task.setCurrentStepIndex(nextIndex);
            if (task.getStatus() == TaskStatus.NOT_STARTED) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
        }
        taskRepository.save(task);
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