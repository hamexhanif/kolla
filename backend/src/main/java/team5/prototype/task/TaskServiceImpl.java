package team5.prototype.task;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team5.prototype.taskstep.Priority;
import team5.prototype.taskstep.TaskStep;
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
    // private final PriorityService priorityService; // Noch nicht implementiert

    public TaskServiceImpl(TaskRepository taskRepository,
                           WorkflowDefinitionRepository definitionRepository,
                           UserRepository userRepository,
                           PriorityService priorityService) {
        this.taskRepository = taskRepository;
        this.definitionRepository = definitionRepository;
        this.userRepository = userRepository;
        // this.priorityService = priorityService;
    }

    @Override
    @Transactional
    public Task createTaskFromDefinition(TaskDto request) { // PARAMETER GEÄNDERT
        WorkflowDefinition definition = definitionRepository.findById(request.getWorkflowDefinitionId()) // ANPASSUNG
                .orElseThrow(() -> new EntityNotFoundException(
                        "WorkflowDefinition %d nicht gefunden".formatted(request.getWorkflowDefinitionId()))); // ANPASSUNG

        if (definition.getSteps().isEmpty()) {
            throw new IllegalStateException("WorkflowDefinition enthält keine Schritte");
        }

        User creator = findUser(request.getCreatorUserId()); // ANPASSUNG

        Task task = Task.builder()
                .title(request.getTitle()) // ANPASSUNG
                .description(request.getDescription()) // ANPASSUNG
                .deadline(request.getDeadline()) // ANPASSUNG
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

        Map<Long, Long> overrides = request.getStepAssignments(); // ANPASSUNG
        List<TaskStep> concreteSteps = buildTaskSteps(task, orderedSteps, overrides);
        task.setTaskSteps(concreteSteps);

        // refreshPriorityForActiveSteps(task); // Diese Methode ist noch auskommentiert
        return taskRepository.save(task);
    }

    // In TaskServiceImpl.java

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

    // --- Private Hilfsmethoden, die nur für die Task-Erstellung relevant sind ---

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
                    .priority(Priority.MEDIUM_TERM); // Vorerst fester Wert
            if (index == 0) {
                builder.assignedAt(now);
            }
            steps.add(builder.build());
        }
        return steps;
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
    @Override
    public List<Task> getAllTasks() {
        // TODO: Implementierung später hinzufügen
        return List.of(); // Gibt eine leere Liste zurück
    }

    @Override
    public Optional<Task> getTaskById(Long taskId) {
        // TODO: Implementierung später hinzufügen
        return Optional.empty(); // Gibt ein leeres Optional zurück
    }
}