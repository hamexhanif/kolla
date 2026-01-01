package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.PriorityService;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepDto;
import team5.prototype.taskstep.TaskStepStatus;
import team5.prototype.user.User;
import team5.prototype.user.UserRepository;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final UserRepository userRepository;
    private final PriorityService priorityService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           WorkflowDefinitionRepository definitionRepository,
                           UserRepository userRepository,
                           PriorityService priorityService) {
        this.taskRepository = taskRepository;
        this.definitionRepository = definitionRepository;
        this.userRepository = userRepository;
        this.priorityService = priorityService;
    }

    @Override
    @Transactional
    public Task createTaskFromDefinition(TaskDto request) {
        WorkflowDefinition definition = definitionRepository.findById(request.getWorkflowDefinitionId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "WorkflowDefinition %d nicht gefunden".formatted(request.getWorkflowDefinitionId())));

        if (definition.getSteps().isEmpty()) {
            throw new IllegalStateException("WorkflowDefinition enthält keine Schritte");
        }

        User creator = findUser(request.getCreatorUserId());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .workflowDefinition(definition)
                .tenant(definition.getTenant())
                .createdBy(creator)
                .status(TaskStatus.NOT_STARTED)
                .currentStepIndex(0)
                .taskSteps(new ArrayList<>())
                .build();

        List<WorkflowStep> orderedSteps = definition.getSteps()
                .stream()
                .sorted(Comparator.comparingInt(WorkflowStep::getSequenceOrder))
                .collect(Collectors.toList());

        Map<Long, Long> overrides = request.getStepAssignments();
        List<TaskStep> concreteSteps = buildTaskSteps(task, orderedSteps, overrides);
        task.setTaskSteps(concreteSteps);

        refreshPriorityForNotCompletedTaskSteps(task);
        return taskRepository.save(task);
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
        if (step.getStatus() == TaskStepStatus.COMPLETED) {
            return;
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

    // NEUE METHODE: Gibt DTOs zurück statt Entities
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasksAsDto() {
        List<Task> tasks = taskRepository.findAll();
        // Konvertierung INNERHALB der Transaction
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // NEUE METHODE: Gibt DTO zurück statt Entity
    @Override
    @Transactional(readOnly = true)
    public Optional<TaskDto> getTaskByIdAsDto(Long taskId) {
        return taskRepository.findById(taskId)
                .map(this::convertToDto);
    }

    // ALTE Methoden bleiben für Kompatibilität
    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    // --- Private Konvertierungs-Hilfsmethoden ---

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
        return dto;
    }

    // --- Private Hilfsmethoden ---

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

        if (task.getStatus() == TaskStatus.NOT_STARTED) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
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
                    .status(index == 0 ? TaskStepStatus.ASSIGNED : TaskStepStatus.WAITING)
                    .priority(Priority.MEDIUM_TERM);
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
                .forEach(step -> step.setPriority(priorityService.calculatePriority(step)));
    }

    private User resolveAssignee(WorkflowStep workflowStep, Map<Long, Long> overrides, Long tenantId) {
        if (overrides != null && overrides.containsKey(workflowStep.getId())) {
            return findUser(overrides.get(workflowStep.getId()));
        }
        String roleName = workflowStep.getRequiredRole().getName();
        return userRepository.findFirstByRoles_NameAndTenant_IdOrderByIdAsc(roleName, tenantId)
                .orElseThrow(() -> new IllegalStateException("Kein Benutzer für Rolle %s gefunden".formatted(roleName)));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User %d nicht gefunden".formatted(userId)));
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }
}