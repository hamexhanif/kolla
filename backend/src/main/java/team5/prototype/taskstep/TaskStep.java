package team5.prototype.taskstep;

import jakarta.persistence.*;
import lombok.*;
import team5.prototype.workflow.step.WorkflowStep;
import team5.prototype.task.Task;
import team5.prototype.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStepStatus status = TaskStepStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(name = "manual_priority")
    private Integer manualPriority;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null && status != TaskStepStatus.WAITING) {
            assignedAt = LocalDateTime.now();
        }
    }
}
