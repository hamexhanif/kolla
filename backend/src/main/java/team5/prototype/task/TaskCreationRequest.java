package team5.prototype.task;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Command object describing how a workflow definition should be instantiated as a concrete task.
 *
 * @param workflowDefinitionId the template identifier
 * @param title                title for the new task
 * @param description          optional detailed description
 * @param deadline             absolute deadline for the task
 * @param creatorUserId        user creating the task
 * @param stepAssignments      optional overrides for assigning workflow steps to users
 */
public record TaskCreationRequest(
        Long workflowDefinitionId,
        String title,
        String description,
        LocalDateTime deadline,
        Long creatorUserId,
        Map<Long, Long> stepAssignments
) {

    public TaskCreationRequest {
        stepAssignments = stepAssignments == null ? Collections.emptyMap() : Map.copyOf(stepAssignments);
    }
}
