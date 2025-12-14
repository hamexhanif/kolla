package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.Priority;
import team5.prototype.entity.Task;
import team5.prototype.entity.TaskStatus;
import team5.prototype.entity.WorkflowStep;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
public class PrioritizationServiceImpl implements PrioritizationService {

    @Override
    public Priority calculatePriority(Task task) {
        // 1. Prüfen, ob eine manuelle Priorität gesetzt ist. Wenn ja, hat diese Vorrang.
        if (task.getOverriddenPriority() != null) {
            return task.getOverriddenPriority();
        }

        // 2. Verbleibende Kalenderzeit bis zur Deadline in Stunden berechnen
        // ZonedDateTime.now() berücksichtigt Zeitzonen, was robuster ist.
        long verbleibendeStunden = Duration.between(ZonedDateTime.now(), task.getDeadline()).toHours();

        // 3. Dauer aller noch nicht erledigten Arbeitsschritte summieren
        int restArbeitInStunden = 0;
        for (WorkflowStep step : task.getSteps()) {
            if (step.getStatus() != TaskStatus.COMPLETED) {
                // Wir greifen auf die Dauer aus der Vorlage (WorkflowStepDefinition) zu
                restArbeitInStunden += step.getWorkflowStepDefinition().getDefaultDurationHours();
            }
        }

        // 4. Pufferzeit berechnen (logischste Interpretation der Anforderung)
        long pufferInStunden = verbleibendeStunden - restArbeitInStunden;

        // 5. Die Regel aus der Projektbeschreibung anwenden
        if (pufferInStunden <= 8) {
            return Priority.IMMEDIATE;
        } else if (pufferInStunden <= 32) {
            return Priority.MEDIUM_TERM;
        } else {
            return Priority.LONG_TERM;
        }
    }
}
