// ERSETZE DEN GESAMTEN INHALT DEINER TaskStepServiceImpl.java MIT DIESEM CODE

package team5.prototype.taskstep;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.ActorDashboardItemDto;
import team5.prototype.notification.NotificationService;
import team5.prototype.security.TenantProvider;
import team5.prototype.task.TaskService;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskStepServiceImpl implements TaskStepService {

    private final TaskStepRepository taskStepRepository;
    private final UserRepository userRepository;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final TenantProvider tenantProvider;

    // Konstruktor-Injection
    public TaskStepServiceImpl(TaskStepRepository taskStepRepository,
                               UserRepository userRepository,
                               TaskService taskService,
                               NotificationService notificationService,
                               TenantProvider tenantProvider) {
        this.taskStepRepository = taskStepRepository;
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.tenantProvider = tenantProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActorDashboardItemDto> getActorDashboardItems(Long userId) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        findUser(userId, tenantId);
        return taskStepRepository.findByAssignedUserIdAndStatusNotAndTask_Tenant_Id(
                        userId, TaskStepStatus.COMPLETED, tenantId)
                .stream()
                .map(this::toActorDashboardItemDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public TaskStep assignTaskStepToUser(Long taskStepId, Long userId) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        TaskStep step = findTaskStep(taskStepId, tenantId);
        User user = findUser(userId, tenantId);
        step.setAssignedUser(user);
        step.setStatus(TaskStepStatus.ASSIGNED);
        if (step.getAssignedAt() == null) {
            step.setAssignedAt(LocalDateTime.now());
        }
        return taskStepRepository.save(step);
    }
    @Override
    @Transactional
    public TaskStepDto setManualPriorityAndConvertToDto(Long taskStepId, int manualPriority) {
        TaskStep step = findTaskStep(taskStepId, tenantProvider.getCurrentTenantId());
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

    @Transactional(readOnly = true)
    public List<TaskStep> getActiveTaskStepsByUser(Long userId) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        findUser(userId, tenantId);
        return taskStepRepository.findByAssignedUserIdAndStatusNotAndTask_Tenant_Id(
                userId, TaskStepStatus.COMPLETED, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActorDashboardItemDto> getActorDashboardItems(Long userId,
                                                              TaskStepStatus status,
                                                              Priority priority,
                                                              String query) {
        Long tenantId = tenantProvider.getCurrentTenantId();
        findUser(userId, tenantId);
        Stream<TaskStep> steps = taskStepRepository
                .findByAssignedUserIdAndStatusNotAndTask_Tenant_Id(userId, TaskStepStatus.COMPLETED, tenantId)
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

    // DIESE METHODE IST KORREKT UND BLEIBT
    @Override
    @Transactional
    public void completeTaskStep(Long taskId, Long taskStepId, Long userId) {
        taskService.completeStep(taskId, taskStepId, userId);
    }

    private TaskStep findTaskStep(Long id, Long tenantId) {
        return taskStepRepository.findByIdAndTask_Tenant_Id(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("TaskStep %d nicht gefunden".formatted(id)));
    }

    private User findUser(Long id, Long tenantId) {
        return userRepository.findByIdAndTenant_Id(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("User %d nicht gefunden".formatted(id)));
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

}
