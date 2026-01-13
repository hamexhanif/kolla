package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.dto.ManagerDashboardDto;
import team5.prototype.dto.ManagerTaskRowDto;
import team5.prototype.dto.TaskDetailsDto;
import team5.prototype.dto.TaskDetailsStepDto;
import team5.prototype.notification.NotificationService;
import team5.prototype.role.Role;
import team5.prototype.taskstep.*;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskStepRepository taskStepRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final UserRepository userRepository;
    private final PriorityService priorityService;
    private final NotificationService notificationService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskStepRepository taskStepRepository,
                           WorkflowDefinitionRepository definitionRepository,
                           UserRepository userRepository,
                           PriorityService priorityService,
                           NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.taskStepRepository = taskStepRepository;
        this.definitionRepository = definitionRepository;
        this.userRepository = userRepository;
        this.priorityService = priorityService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TaskDto createTaskFromDefinition(TaskDto request) {
        WorkflowDefinition definition = definitionRepository.findById(request.getWorkflowDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "WorkflowDefinition %d nicht gefunden".formatted(request.getWorkflowDefinitionId())));

        if (definition.getSteps().isEmpty()) {
            throw new IllegalStateException("WorkflowDefinition enthaelt keine Schritte");
        }

        List<WorkflowStep> orderedSteps = definition.getSteps()
                .stream()
                .sorted(Comparator.comparingInt(WorkflowStep::getSequenceOrder))
                .collect(Collectors.toList());

        // Calculate total duration from all workflow steps
        long totalDurationHours = orderedSteps.stream()
                .mapToLong(WorkflowStep::getDurationHours)
                .sum();

        LocalDateTime minimumDeadline = LocalDateTime.now().plusHours(totalDurationHours);
        if (request.getDeadline().isBefore(minimumDeadline)) {
            throw new IllegalArgumentException(
                    String.format("Deadline muss mindestens %s sein (jetzt + %d Stunden)",
                            minimumDeadline, totalDurationHours)
            );
        }

        User creator = findUser(request.getCreatorUserId());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .workflowDefinition(definition)
                .tenant(definition.getTenant())
                .createdBy(creator)
                .status(TaskStatus.IN_PROGRESS)
                .currentStepIndex(0)
                .taskSteps(new ArrayList<>())
                .build();

        Map<Long, Long> overrides = request.getStepAssignments();
        List<TaskStep> concreteSteps = buildTaskSteps(task, orderedSteps, overrides);
        task.setTaskSteps(concreteSteps);

        refreshPriorityForNotCompletedTaskSteps(task);
        taskRepository.save(task);
        return convertToDto(task);
    }

    @Override
    @Transactional
    public void completeStep(Long taskId, Long stepId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task %d nicht gefunden".formatted(taskId)));

        TaskStep step = task.getTaskSteps()
                .stream()
                .filter(ts -> Objects.equals(ts.getId(), stepId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("TaskStep %d nicht gefunden".formatted(stepId)));

        if (!Objects.equals(step.getAssignedUser().getId(), userId)) {
            throw new IllegalArgumentException("Benutzer %d ist nicht dem Arbeitsschritt zugeordnet".formatted(userId));
        }
        // ===================================================================
        // 2. NEUE PRÜFUNG: Hat der zugewiesene Benutzer die erforderliche Rolle?
        // ===================================================================
        Role requiredRole = step.getWorkflowStep().getRequiredRole();
        boolean userHasRequiredRole = step.getAssignedUser().getRoles().stream()
                .anyMatch(userRole -> userRole.getId().equals(requiredRole.getId()));

        if (!userHasRequiredRole) {
            throw new IllegalStateException(String.format(
                    "Benutzer %d hat nicht die erforderliche Rolle '%s' für diesen Arbeitsschritt.",
                    userId, requiredRole.getName()
            ));
        }
        if (step.getStatus() == TaskStepStatus.COMPLETED) {
            return; // Bereits erledigt, nichts zu tun.
        }
        if (step.getStatus() == TaskStepStatus.WAITING) {
            throw new IllegalStateException("Arbeitsschritt ist noch nicht aktiv.");
        }

        step.setStatus(TaskStepStatus.COMPLETED);
        step.setCompletedAt(now());
        step.setStartedAt(Optional.ofNullable(step.getStartedAt()).orElse(step.getAssignedAt()));

        moveToNextStep(task, step);
        refreshPriorityForNotCompletedTaskSteps(task);
        taskRepository.save(task);

        // ===================================================================
        // NEU HINZUGEFUEGT: Benachrichtigung senden
        // ===================================================================
        Map<String, Object> notificationPayload = Map.of(
                "message", String.format("Arbeitsschritt '%s' (ID: %d) wurde abgeschlossen.", step.getWorkflowStep().getName(), stepId),
                "taskId", taskId,
                "completedStepId", stepId,
                "newOverallTaskStatus", task.getStatus().name()
        );

        notificationService.sendTaskUpdateNotification(taskId, notificationPayload);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskProgress getTaskProgress(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task %d nicht gefunden".formatted(taskId)));

        int totalSteps = task.getTaskSteps().size();
        int completedSteps = (int) task.getTaskSteps().stream()
                .filter(step -> step.getStatus() == TaskStepStatus.COMPLETED)
                .count();

        return new TaskProgress(
                task.getId(),
                task.getTitle(),
                task.getDeadline(),
                totalSteps,
                completedSteps,
                task.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDetailsDto getTaskDetails(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task %d nicht gefunden".formatted(taskId)));
        int totalSteps = task.getTaskSteps().size();
        int completedSteps = (int) task.getTaskSteps().stream()
                .filter(step -> step.getStatus() == TaskStepStatus.COMPLETED)
                .count();
        Priority priority = priorityService.calculatePriority(task);
        List<TaskDetailsStepDto> stepDtos = task.getTaskSteps().stream()
                .sorted(Comparator.comparingInt(step -> step.getWorkflowStep().getSequenceOrder()))
                .map(this::toTaskDetailsStep)
                .toList();
        return new TaskDetailsDto(task.getId(), task.getTitle(), priority, task.getDeadline(),
                completedSteps, totalSteps, stepDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public ManagerDashboardDto getManagerDashboard() {
        List<Task> tasks = taskRepository.findAll();
        LocalDate today = LocalDate.now();
        long openTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .count();
        long overdueTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> task.getDeadline().isBefore(today.atStartOfDay()))
                .count();
        long dueTodayTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> task.getDeadline().toLocalDate().equals(today))
                .count();
        List<ManagerTaskRowDto> rows = tasks.stream()
                .map(this::toManagerTaskRow)
                .toList();
        return new ManagerDashboardDto(openTasks, overdueTasks, dueTodayTasks, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasksAsDto() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskDto> getTaskByIdAsDto(Long taskId) {
        return taskRepository.findById(taskId)
                .map(this::convertToDto);
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDeadline(task.getDeadline());
        if (task.getStatus() != null) {
            dto.setStatus(task.getStatus().name());
        }

        if (task.getTaskSteps() != null) {
            List<TaskStepDto> stepDtos = task.getTaskSteps().stream()
                    .map(this::convertStepToDto)
                    .collect(Collectors.toList());
            dto.setSteps(stepDtos);
        }
        dto.setPriority(priorityService.calculatePriority(task));
        return dto;
    }

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
        dto.setPriority(String.valueOf(step.getPriority()));
        return dto;
    }

    private void moveToNextStep(Task task, TaskStep completedStep) {
        List<TaskStep> steps = task.getTaskSteps();
        int completedIndex = steps.indexOf(completedStep);
        int nextIndex = completedIndex + 1;

        if (nextIndex >= steps.size()) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(now());
            task.setCurrentStepIndex(nextIndex);
            return;
        }

        TaskStep nextStep = steps.get(nextIndex);
        nextStep.setStatus(TaskStepStatus.ASSIGNED);
        nextStep.setAssignedAt(now());
        task.setCurrentStepIndex(nextIndex);
    }

    private List<TaskStep> buildTaskSteps(Task task, List<WorkflowStep> orderedSteps, Map<Long, Long> overrides) {
        List<TaskStep> steps = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int index = 0; index < orderedSteps.size(); index++) {
            WorkflowStep workflowStep = orderedSteps.get(index);
            User assignee = resolveAssignee(workflowStep, overrides, task.getTenant().getId());

            TaskStep.TaskStepBuilder builder = TaskStep.builder()
                    .task(task)
                    .workflowStep(workflowStep)
                    .assignedUser(assignee)
                    .status(index == 0 ? TaskStepStatus.ASSIGNED : TaskStepStatus.WAITING);

            if (index == 0) {
                builder.assignedAt(now);
            }
            steps.add(builder.build());
        }
        return steps;
    }

    private void refreshPriorityForNotCompletedTaskSteps(Task task) {
        task.getTaskSteps().stream()
                .filter(step -> step.getStatus() != TaskStepStatus.COMPLETED)
                .filter(step -> step.getManualPriority() == null)
                .forEach(step -> step.setPriority(priorityService.calculatePriority(step)));
    }

    private User resolveAssignee(WorkflowStep workflowStep, Map<Long, Long> overrides, Long tenantId) {
        if (overrides != null && overrides.containsKey(workflowStep.getId())) {
            return findUser(overrides.get(workflowStep.getId()));
        }

        String roleName = workflowStep.getRequiredRole().getName();
        List<User> availableUsers = userRepository.findActiveUsersByRoleAndTenant(roleName, tenantId);

        if (availableUsers.isEmpty()) {
            throw new IllegalStateException("Kein Benutzer fuer Rolle %s gefunden".formatted(roleName));
        }

        return findBestAssignee(availableUsers);
    }

    private User findBestAssignee(List<User> candidates) {
        Map<User, List<TaskStep>> userTaskMap = candidates.stream()
                .collect(Collectors.toMap(
                        user -> user,
                        user -> taskStepRepository.findActiveTaskStepsByUser(user.getId())
                ));

        Optional<User> availableUser = userTaskMap.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .findFirst();

        if (availableUser.isPresent()) {
            return availableUser.get();
        }

        return userTaskMap.entrySet().stream()
                .sorted((entry1, entry2) -> {
                    List<TaskStep> tasks1 = entry1.getValue();
                    List<TaskStep> tasks2 = entry2.getValue();

                    int taskCountComparison = Integer.compare(tasks1.size(), tasks2.size());
                    if (taskCountComparison != 0) {
                        return taskCountComparison;
                    }

                    LocalDateTime deadline1 = tasks1.stream()
                            .map(ts -> ts.getTask().getDeadline())
                            .max(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.MIN);

                    LocalDateTime deadline2 = tasks2.stream()
                            .map(ts -> ts.getTask().getDeadline())
                            .max(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.MIN);

                    return deadline2.compareTo(deadline1);
                })
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kein verfuegbarer Benutzer gefunden"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User %d nicht gefunden".formatted(userId)));
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private Priority resolveTaskPriority(Task task) {
        // Finde den ersten, noch nicht abgeschlossenen Arbeitsschritt
        Optional<TaskStep> nextStepOpt = task.getTaskSteps().stream()
                .filter(step -> step.getStatus() != TaskStepStatus.COMPLETED)
                .min(Comparator.comparingInt(step -> step.getWorkflowStep().getSequenceOrder()));

        // Wenn ein nächster Schritt existiert...
        if (nextStepOpt.isPresent()) {
            // rufe den PriorityService auf, um die Priorität in Echtzeit neu zu berechnen.
            return priorityService.calculatePriority(nextStepOpt.get());
        }

        // Wenn alle Schritte erledigt sind, hat die Aufgabe keine Priorität mehr.
        return null;
    }

    private ManagerTaskRowDto toManagerTaskRow(Task task) {
        int totalSteps = task.getTaskSteps().size();
        int completedSteps = (int) task.getTaskSteps().stream()
                .filter(step -> step.getStatus() == TaskStepStatus.COMPLETED)
                .count();
        Priority priority = resolveTaskPriority(task);
        return new ManagerTaskRowDto(task.getId(), task.getTitle(), priority, completedSteps, totalSteps);
    }

    private TaskDetailsStepDto toTaskDetailsStep(TaskStep step) {
        String assigneeName = formatAssigneeName(step.getAssignedUser());
        LocalDateTime dueDate = resolveStepDueDate(step);

        // Wir berechnen die Priorität für jeden einzelnen Schritt neu,
        // damit die Anzeige in der Schrittliste auch immer aktuell ist.
        Priority currentStepPriority = priorityService.calculatePriority(step);

        return new TaskDetailsStepDto(
                step.getId(),
                step.getWorkflowStep().getName(),
                step.getStatus(),
                assigneeName,
                dueDate,
                currentStepPriority
        );
    }

    private LocalDateTime resolveStepDueDate(TaskStep step) {
        LocalDateTime taskDeadline = step.getTask().getDeadline();
        if (taskDeadline == null) {
            return null;
        }

        List<WorkflowStep> allWorkflowSteps = step.getTask().getWorkflowDefinition().getSteps().stream()
                .sorted(Comparator.comparingInt(WorkflowStep::getSequenceOrder))
                .toList();

        int currentStepSequence = step.getWorkflowStep().getSequenceOrder();

        long remainingDurationHours = allWorkflowSteps.stream()
                .filter(ws -> ws.getSequenceOrder() > currentStepSequence)
                .mapToLong(WorkflowStep::getDurationHours)
                .sum();

        return taskDeadline.minusHours(remainingDurationHours);
    }

    private String formatAssigneeName(User user) {
        if (user == null) {
            return null;
        }
        String first = Optional.ofNullable(user.getFirstName()).orElse("").trim();
        String last = Optional.ofNullable(user.getLastName()).orElse("").trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? user.getUsername() : full;
    }

}
