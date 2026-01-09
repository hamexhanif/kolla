package team5.prototype.workflow.step;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import team5.prototype.role.Role;
import team5.prototype.workflow.definition.WorkflowDefinition;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    @JsonBackReference
    private WorkflowDefinition workflowDefinition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "required_role_id", nullable = false)
    private Role requiredRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
