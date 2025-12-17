package team5.prototype.task;

import org.springframework.stereotype.Service;
import team5.prototype.workflow.definition.WorkflowDefinition;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;
import team5.prototype.taskstep.TaskStep;
import team5.prototype.taskstep.TaskStepStatus; // Annahme, dass diese Enum existiert
import team5.prototype.workflow.step.WorkflowStep; // Die Vorlage für einen Schritt

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    // private final PrioritizationService prioritizationService; // Wird später hinzugefügt

    public TaskServiceImpl(TaskRepository taskRepository,
                           WorkflowDefinitionRepository definitionRepository) {
        this.taskRepository = taskRepository;
        this.definitionRepository = definitionRepository;
        // this.prioritizationService = prioritizationService;
    }

    @Override
    public Task createTaskFromDefinition(Long definitionId, String title) {
        // 1. Lade die WorkflowDefinition-Vorlage
        WorkflowDefinition definition = definitionRepository.findById(definitionId)
                .orElseThrow(() -> new RuntimeException("WorkflowDefinition mit ID " + definitionId + " nicht gefunden!"));

        // 2. Erstelle eine neue, konkrete Task
        Task newTask = new Task();
        newTask.setTitle(title);
        newTask.setWorkflowDefinition(definition);
        newTask.setStatus(TaskStatus.NOT_STARTED); // Anfangsstatus
        // TODO: Deadline muss noch gesetzt werden.

        // 3. Erstelle konkrete TaskSteps aus den WorkflowSteps der Vorlage
        List<TaskStep> taskSteps = new ArrayList<>();
        for (WorkflowStep stepTemplate : definition.getSteps()) {
            TaskStep newTaskStep = new TaskStep();
            newTaskStep.setWorkflowStep(stepTemplate); // Verknüpfe den konkreten Schritt mit seiner Vorlage
            newTaskStep.setStatus(TaskStepStatus.NOT_STARTED); // Status des Einzelschritts
            newTaskStep.setTask(newTask);
            taskSteps.add(newTaskStep);
        }
        newTask.setTaskSteps(taskSteps);

        // 4. Speichere die neue Task mit all ihren Schritten
        return taskRepository.save(newTask);
    }

    // Implementierung der CRUD-Methoden
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}