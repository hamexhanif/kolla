package team5.prototype.task;

import team5.prototype.taskstep.Priority;

public interface PriorityService {

    /**
     * Berechnet die Priorität für eine gegebene Aufgabe.
     * @param task Die Aufgabe, für die die Priorität berechnet werden soll.
     * @return Die berechnete Priorität (IMMEDIATE, MEDIUM_TERM, LONG_TERM).
     */
    Priority calculatePriority(Task task);
}
