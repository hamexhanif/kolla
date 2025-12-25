package team5.prototype.taskstep;

public interface PriorityService {

    /**
     * Berechnet die Prioritaet fuer einen gegebenen TaskStep.
     * @param taskStep Der TaskStep, fuer den die Prioritaet berechnet werden soll.
     * @return Die berechnete Prioritaet (IMMEDIATE, MEDIUM_TERM, LONG_TERM).
     */
    Priority calculatePriority(TaskStep taskStep);
}
