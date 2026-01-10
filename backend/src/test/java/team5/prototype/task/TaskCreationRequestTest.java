package team5.prototype.task;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskCreationRequestTest {

    @Test
    void constructorDefaultsStepAssignmentsToEmptyMap() {
        TaskCreationRequest request = new TaskCreationRequest(
                1L,
                "title",
                "desc",
                LocalDateTime.now(),
                2L,
                null
        );

        assertThat(request.stepAssignments()).isNotNull();
        assertThat(request.stepAssignments()).isEmpty();
    }

    @Test
    void constructorCopiesStepAssignments() {
        Map<Long, Long> assignments = new HashMap<>();
        assignments.put(1L, 2L);

        TaskCreationRequest request = new TaskCreationRequest(
                1L,
                "title",
                "desc",
                LocalDateTime.now(),
                2L,
                assignments
        );

        assertThat(request.stepAssignments()).containsEntry(1L, 2L);
        assertThatThrownBy(() -> request.stepAssignments().put(3L, 4L))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
