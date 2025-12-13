package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.Task;
import team5.prototype.repository.TaskRepository;
import team5.prototype.repository.WorkflowDefinitionRepository;

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
        // TODO: Logik hier später implementieren (siehe Sequenzdiagramm)
        System.out.println("LOG: Erstelle Task aus Definition " + definitionId);
        return null; // Vorerst nur ein Platzhalter
    }

    @Override
    public void completeStep(Long taskId, Long stepId, String userId) {
        // TODO: Logik hier später implementieren (siehe Sequenzdiagramm)
        System.out.println("LOG: Schließe Schritt " + stepId + " für Task " + taskId + " ab");
    }
}