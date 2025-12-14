package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.Task;
import team5.prototype.entity.TaskStatus;
import team5.prototype.entity.WorkflowDefinition;
import team5.prototype.entity.WorkflowStep;
import team5.prototype.repository.TaskRepository;
import team5.prototype.repository.WorkflowDefinitionRepository;

import java.util.ArrayList;
import java.util.List;

@Service // Wichtig: Sagt Spring, dass dies eine Service-Klasse ist
public class TaskServiceImpl implements TaskService {

    // Abhängigkeiten, die wir später benötigen werden
    private final TaskRepository taskRepository;
    private final WorkflowDefinitionRepository definitionRepository;
    private final PriorityService priorityService;

    // Konstruktor-Injection: Spring wird uns automatisch die benötigten Beans geben
    public TaskServiceImpl(TaskRepository taskRepository,
                           WorkflowDefinitionRepository definitionRepository,
                           PriorityService priorityService) {
        this.taskRepository = taskRepository;
        this.definitionRepository = definitionRepository;
        this.priorityService = priorityService;
    }

    @Override
    public Task createTaskFromDefinition(Long definitionId, String title) {
        // Schritt 1: Lade die Vorlage (die WorkflowDefinition) aus der Datenbank.
        WorkflowDefinition definition = definitionRepository.findById(definitionId)
                .orElseThrow(() -> new IllegalArgumentException("WorkflowDefinition mit ID " + definitionId + " nicht gefunden!"));

        // Schritt 2: Erstelle ein neues, leeres Task-Objekt.
        Task newTask = new Task();
        newTask.setTitle(title);
        newTask.setWorkflowDefinition(definition);
        // TODO: Deadline muss noch sinnvoll gesetzt werden.

        // Schritt 3: Erstelle die konkreten Arbeitsschritte (WorkflowSteps)
        List<WorkflowStep> steps = new ArrayList<>();

        // KORREKTUR HIER: Wir iterieren über die 'steps'-Liste der WorkflowDefinition
        if (definition.getSteps() != null) { // Sicherheitsprüfung
            for (WorkflowStep stepTemplate : definition.getSteps()) {
                WorkflowStep newStep = new WorkflowStep();

                // Wir müssen die relevanten Daten aus der Vorlage kopieren
                // newStep.setName(stepTemplate.getName()); // Annahme, dass es ein 'name' Feld gibt

                newStep.setStatus(TaskStatus.NOT_STARTED);
                newStep.setTask(newTask);
                // newStep.setWorkflowDefinition(definition); // Dies wird jetzt durch die Task-Beziehung abgedeckt

                steps.add(newStep);
            }
        }
        newTask.setSteps(steps);

        // Schritt 4: Speichere die neue, vollständige Task.
        return taskRepository.save(newTask);
    }


    @Override
    public void completeStep(Long taskId, Long stepId, String userId) {
        // TODO: Logik hier später implementieren
        System.out.println("Schließe Schritt " + stepId + " für Task " + taskId + " ab");
    }
}