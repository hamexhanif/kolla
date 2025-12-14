package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.*;
import team5.prototype.repository.TaskRepository;
import team5.prototype.repository.WorkflowDefinitionRepository;
import team5.prototype.entity.TaskStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    // Abhängigkeiten, die dieser Service benötigt
    private final TaskRepository taskRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final PrioritizationService prioritizationService;

    // Konstruktor-Injection: Spring liefert uns die benötigten Objekte automatisch
    public TaskServiceImpl(TaskRepository taskRepository,
                           WorkflowDefinitionRepository definitionRepository,
                           PrioritizationService prioritizationService) {
        this.taskRepository = taskRepository;
        this.definitionRepository = definitionRepository;
        this.prioritizationService = prioritizationService;
    }

    @Override
    public Task createTaskFromDefinition(Long definitionId, String title) {
        // Logik aus dem Sequenzdiagramm:

        // 1. Lade die Vorlage
        WorkflowDefinition definition = definitionRepository.findById(definitionId)
                .orElseThrow(() -> new RuntimeException("WorkflowDefinition nicht gefunden!"));

        // 2. Erstelle eine neue Task
        Task newTask = new Task();
        newTask.setTitle(title);
        newTask.setWorkflowDefinition(definition);
        // TODO: Deadline muss hier noch gesetzt werden, z.B. über einen Parameter

        // 3. Erstelle die konkreten Arbeitsschritte aus den Definitionen
        List<WorkflowStep> steps = new ArrayList<>();
        for (WorkflowStepDefinition stepDef : definition.getStepDefinitions()) {
            WorkflowStep newStep = new WorkflowStep();
            newStep.setWorkflowStepDefinition(stepDef);
            newStep.setStatus(TaskStatus.NOT_STARTED);
            newStep.setTask(newTask); // Wichtig für die bidirektionale Beziehung
            steps.add(newStep);
        }
        newTask.setSteps(steps);

        // 4. Speichere die neue Task mit all ihren Steps
        return taskRepository.save(newTask);
    }

    @Override
    public void completeStep(Long taskId, Long stepId, String userId) {
        // Logik aus dem Sequenzdiagramm:

        // 1. Lade die Task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task nicht gefunden!"));

        // 2. Finde den spezifischen Arbeitsschritt
        WorkflowStep stepToComplete = task.getSteps().stream()
                .filter(step -> step.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("WorkflowStep nicht gefunden!"));

        // 3. (Optionaler Sicherheits-Check)
        // if (!stepToComplete.getAssignedUser().getId().equals(userId)) { ... }

        // 4. Setze den Status
        stepToComplete.setStatus(TaskStatus.COMPLETED);

        // 5. Finde & weise den nächsten Schritt zu (vereinfachte Logik)
        // ... (Hier kommt die Logik zum Finden des nächsten Schritts in der Definition) ...

        // 6. Berechne die Priorität neu
        // Da sich die Restarbeit geändert hat, muss die Priorität neu bewertet werden
        // Die neue Priorität wird nicht direkt gesetzt, sondern beim nächsten Abruf berechnet

        // 7. Speichere die geänderte Task
        taskRepository.save(task);

        // 8. TODO: Benachrichtigung senden
        // notificationPublisher.publishProgressUpdate(task);
    }
}