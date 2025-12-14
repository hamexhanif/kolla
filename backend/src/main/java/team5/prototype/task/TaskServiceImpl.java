package team5.prototype.task;

import org.springframework.stereotype.Service;
import team5.prototype.workflow.definition.WorkflowDefinitionRepository;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final WorkflowDefinitionRepository definitionRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           WorkflowDefinitionRepository definitionRepository) {
        this.taskRepository = taskRepository;
        this.definitionRepository = definitionRepository;
    }

    @Override
    public Task createTaskFromDefinition(Long definitionId, String title) {
        // TODO: Logik hier später implementieren
        System.out.println("Erstelle Task aus Definition " + definitionId);
        return null; // Vorerst nur ein Platzhalter
    }

//    @Override
//    public void completeStep(Long taskId, Long stepId, String userId) {
//        // TODO: Logik hier später implementieren
//        System.out.println("Schließe Schritt " + stepId + " für Task " + taskId + " ab");
//    }
}