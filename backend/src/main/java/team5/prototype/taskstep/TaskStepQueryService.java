package team5.prototype.taskstep;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.user.User; // Importiere User, falls für convertToDto benötigt

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) // Gut für reine Lese-Services
public class TaskStepQueryService {

    private final TaskStepRepository taskStepRepository;

    public TaskStepQueryService(TaskStepRepository taskStepRepository) {
        this.taskStepRepository = taskStepRepository;
    }

    // Diese Methode holt die Daten UND konvertiert sie direkt in DTOs
    public List<TaskStepDto> getActiveTaskStepsForUserAsDto(Long userId) {
        return taskStepRepository.findByAssignedUserIdAndStatusNot(userId, TaskStepStatus.COMPLETED)
                .stream()
                .map(this::convertStepToDto)
                .collect(Collectors.toList());
    }

    // Wir kopieren die Konvertierungslogik hierher.
    // Diese Methode ist jetzt privat innerhalb dieses Services.
    private TaskStepDto convertStepToDto(TaskStep step) {
        TaskStepDto dto = new TaskStepDto();
        dto.setId(step.getId());
        if (step.getWorkflowStep() != null) {
            dto.setName(step.getWorkflowStep().getName());
        }
        if (step.getStatus() != null) {
            dto.setStatus(step.getStatus().name());
        }
        if (step.getAssignedUser() != null) {
            dto.setAssignedUsername(step.getAssignedUser().getUsername());
        }
        if (step.getPriority() != null) {
            dto.setPriority(step.getPriority().name());
        }
        return dto;
    }
}