// ERSETZE DEN GESAMTEN INHALT DEINER TaskStepServiceImpl.java MIT DIESEM CODE

package team5.prototype.taskstep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.notification.NotificationService;
import team5.prototype.task.TaskService;
import team5.prototype.tenant.TenantContext;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskStepServiceImpl implements TaskStepService {

    private final TaskStepRepository taskStepRepository;
    private final TaskService taskService;
    private final NotificationService notificationService;

    // @Lazy hier, um die zirkuläre Abhängigkeit zu lösen
    public TaskStepServiceImpl(TaskStepRepository taskStepRepository,
                               @Lazy TaskService taskService,
                               NotificationService notificationService) {
        this.taskStepRepository = taskStepRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActorDashboardItemDto> getActorDashboardItems(Long userId, TaskStepStatus status, Priority priority, String query) {
        Long tenantId = currentTenantId();
        Stream<TaskStep> steps = taskStepRepository
                .findByAssignedUserIdAndTask_Tenant_IdAndStatusNot(userId, tenantId, TaskStepStatus.COMPLETED)
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
        return steps.map(this::toActorDashboardItemDto).toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<ActorDashboardItemDto> getActorDashboardItems(Long userId) {
        return taskStepRepository.findByAssignedUserIdAndTask_Tenant_IdAndStatusNot(userId, currentTenantId(), TaskStepStatus.COMPLETED)
                .stream()
                .map(this::toActorDashboardItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskStepDto setManualPriorityAndConvertToDto(Long taskStepId, int manualPriority) {
        TaskStep step = findTaskStep(taskStepId);
        step.setManualPriority(manualPriority);
        step.setPriority(mapManualPriority(manualPriority));
        TaskStep savedStep = taskStepRepository.save(step);

        Map<String, Object> payload = Map.of(
                "message", String.format("Die Priorität für Schritt '%s' wurde manuell geändert.", savedStep.getWorkflowStep().getName()),
                "taskStepId", savedStep.getId(),
                "newPriority", savedStep.getPriority().name()
        );
        notificationService.sendTaskUpdateNotification(savedStep.getTask().getId(), payload);

        return convertToDto(savedStep);
    }

    @Override
    @Transactional
    public void completeTaskStep(Long taskId, Long taskStepId, Long userId) {
        taskService.completeStep(taskId, taskStepId, userId);
    }

    @Override
    public List<TaskStep> getAllTaskStepsByUserId(Long userId) {
        return taskStepRepository.findAllByAssignedUserIdAndTask_Tenant_Id(userId, currentTenantId());
    }

    // --- Private Hilfsmethoden ---

    private TaskStep findTaskStep(Long id) {
        return taskStepRepository.findByIdAndTask_Tenant_Id(id, currentTenantId())
                .orElseThrow(() -> new EntityNotFoundException("TaskStep %d nicht gefunden".formatted(id)));
    }

    private Priority mapManualPriority(int manualPriority) {
        if (manualPriority <= 1) return Priority.IMMEDIATE;
        if (manualPriority == 2) return Priority.MEDIUM_TERM;
        return Priority.LONG_TERM;
    }

    // Konvertiert zu dem spezifischen DTO für das Akteur-Dashboard
    private ActorDashboardItemDto toActorDashboardItemDto(TaskStep step) {
        return new ActorDashboardItemDto(
                step.getTask().getId(), step.getTask().getTitle(), step.getTask().getDeadline(),
                step.getTask().getStatus(), step.getId(), step.getWorkflowStep().getName(),
                step.getWorkflowStep().getSequenceOrder(), step.getStatus(),
                step.getPriority(), step.getAssignedAt()
        );
    }

    // Konvertiert zu dem allgemeinen TaskStepDto
    private TaskStepDto convertToDto(TaskStep step) {
        TaskStepDto dto = new TaskStepDto();
        dto.setId(step.getId());
        if (step.getWorkflowStep() != null) dto.setName(step.getWorkflowStep().getName());
        if (step.getStatus() != null) dto.setStatus(step.getStatus().name());
        if (step.getAssignedUser() != null) dto.setAssignedUsername(step.getAssignedUser().getUsername());
        if (step.getPriority() != null) dto.setPriority(step.getPriority().name());
        return dto;
    }
    private boolean containsIgnoreCase(String value, String needleLower) {
        if (value == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(needleLower);
    }

    private Long currentTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new EntityNotFoundException("Kein Tenant-Kontext vorhanden");
        }
        return tenantId;
    }

}
