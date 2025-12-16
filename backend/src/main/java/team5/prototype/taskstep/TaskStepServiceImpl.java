package team5.prototype.taskstep;

import org.springframework.stereotype.Service;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;

import java.util.List;

@Service
public class TaskStepServiceImpl implements TaskStepService {

    private final TaskStepRepository taskStepRepository;
    private final UserRepository userRepository;

    public TaskStepServiceImpl(TaskStepRepository taskStepRepository, UserRepository userRepository) {
        this.taskStepRepository = taskStepRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void assignTaskStepToUser(Long taskStepId, Long userId) {
        TaskStep taskStep = taskStepRepository.findById(taskStepId)
                .orElseThrow(() -> new RuntimeException("TaskStep nicht gefunden!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden!"));

        taskStep.setAssignedUser(user);
        taskStepRepository.save(taskStep);
    }

    @Override
    public void completeTaskStep(Long taskStepId, String userId) { // KORRIGIERT: userId hinzugefügt
        TaskStep taskStep = taskStepRepository.findById(taskStepId)
                .orElseThrow(() -> new RuntimeException("TaskStep nicht gefunden!"));

        // TODO: Sicherheitscheck, ob der userId der zugewiesene User ist

        taskStep.setStatus(TaskStepStatus.COMPLETED);
        taskStepRepository.save(taskStep);

        // TODO: Benachrichtigung auslösen und nächsten Schritt im TaskService anstoßen
    }

    @Override
    public List<TaskStep> getTaskStepsByUserId(Long userId) {
        // TODO: Eine benutzerdefinierte Methode im TaskStepRepository erstellen
        return List.of();
    }
}