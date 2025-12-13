package team5.prototype.service;

import team5.prototype.entity.Priority;
import team5.prototype.entity.Task;

public interface PrioritizationService {

    /**
     * Berechnet die Priorität für eine gegebene Aufgabe.
     * @param task Die Aufgabe, für die die Priorität berechnet werden soll.
     * @return Die berechnete Priorität.
     */
    Priority calculatePriority(Task task);
}