package team5.prototype.service;

import team5.prototype.entity.Priority;
import team5.prototype.entity.Task;

public interface PrioritizationService {

    /**
     * Berechnet die Priorität für eine gegebene Aufgabe.
     * @param task Die Aufgabe, für die die Priorität berechnet werden soll.
     * @return Die berechnete Priorität (IMMEDIATE, MEDIUM_TERM, LONG_TERM).
     */
    Priority calculatePriority(Task task);
}