package team5.prototype.service;

import org.springframework.stereotype.Service;
import team5.prototype.entity.Priority;
import team5.prototype.entity.Task;

@Service
public class PrioritizationServiceImpl implements PrioritizationService {

    @Override
    public Priority calculatePriority(Task task) {
        // TODO: Die Berechnungslogik aus der Projektbeschreibung hier implementieren
        System.out.println("LOG: Berechne Priorität für Task " + task.getId());
        return Priority.MEDIUM_TERM; // Vorerst nur ein fester Wert
    }
}