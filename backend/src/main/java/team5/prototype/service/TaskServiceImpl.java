package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.Task;
import team5.prototype.repository.TaskRepository;
import team5.prototype.repository.WorkflowDefinitionRepository;

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
        // TODO: Logik hier später implementieren
        System.out.println("Erstelle Task aus Definition " + definitionId);
        return null; // Vorerst nur ein Platzhalter
    }

    @Override
    public void completeStep(Long taskId, Long stepId, String userId) {
        // TODO: Logik hier später implementieren
        System.out.println("Schließe Schritt " + stepId + " für Task " + taskId + " ab");
    }
}