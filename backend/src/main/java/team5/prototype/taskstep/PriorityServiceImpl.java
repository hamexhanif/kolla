package team5.prototype.taskstep;

import org.springframework.stereotype.Service;
import team5.prototype.task.Task;
import team5.prototype.workflow.step.WorkflowStep;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PriorityServiceImpl implements PriorityService {

    @Override
    public Priority calculatePriority(TaskStep taskStep) {
        LocalDateTime now = LocalDateTime.now();

        // ===================================================================
        // NEUE LOGIK (Vorschlag von Hanif):
        // 1. Berechne zuerst die individuelle Deadline für genau diesen Arbeitsschritt.
        // ===================================================================
        LocalDateTime stepDueDate = calculateStepDueDate(taskStep);

        // Wenn der Schritt keine Deadline hat (z.B. weil die Task keine hat), können wir keine Priorität berechnen.
        // Als sicherer Fallback wird die alte Logik verwendet.
        if (stepDueDate == null) {
            return calculatePriorityByTaskDeadline(taskStep.getTask());
        }

        // ===================================================================
        // 2. Wende die Prioritätsregel (<8h, 8-32h, >32h) auf die Deadline des Schritts an.
        // ===================================================================
        long hoursUntilStepDueDate = Duration.between(now, stepDueDate).toHours();

        if (hoursUntilStepDueDate <= 8) {
            return Priority.IMMEDIATE;
        }
        if (hoursUntilStepDueDate <= 32) {
            return Priority.MEDIUM_TERM;
        }
        return Priority.LONG_TERM;
    }

    @Override
    public Priority calculatePriority(Task task) {
        return calculatePriorityByTaskDeadline(task);
    }

    /**
     * HILFSMETHODE: Berechnet die individuelle Deadline für einen TaskStep,
     * indem von der finalen Task-Deadline die Dauer aller nachfolgenden Schritte abgezogen wird.
     * (Diese Logik wurde aus dem TaskServiceImpl hierher übernommen, um sie wiederzuverwenden).
     */
    private LocalDateTime calculateStepDueDate(TaskStep step) {
        Task task = step.getTask();
        if (task.getDeadline() == null) {
            return null;
        }

        // Hole alle Schritte der Workflow-Vorlage, sortiert nach Reihenfolge
        List<WorkflowStep> allWorkflowSteps = task.getWorkflowDefinition().getSteps().stream()
                .sorted(Comparator.comparingInt(WorkflowStep::getSequenceOrder))
                .toList();

        int currentStepSequence = step.getWorkflowStep().getSequenceOrder();

        // Berechne die Gesamtdauer aller Schritte, die NACH dem aktuellen kommen
        long durationOfSubsequentSteps = allWorkflowSteps.stream()
                .filter(ws -> ws.getSequenceOrder() > currentStepSequence)
                .mapToLong(WorkflowStep::getDurationHours)
                .sum();

        // Die Deadline des Schritts ist die finale Deadline minus der Dauer der Folgeschritte
        return task.getDeadline().minusHours(durationOfSubsequentSteps);
    }

    /**
     * Fallback-Methode, die die alte Logik verwendet, falls die Step-Deadline nicht berechnet werden kann.
     */
    private Priority calculatePriorityByTaskDeadline(Task task) {
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilTaskDeadline = Duration.between(now, task.getDeadline()).toHours();

        if (hoursUntilTaskDeadline <= 8) {
            return Priority.IMMEDIATE;
        }
        if (hoursUntilTaskDeadline <= 32) {
            return Priority.MEDIUM_TERM;
        }
        return Priority.LONG_TERM;
    }
}