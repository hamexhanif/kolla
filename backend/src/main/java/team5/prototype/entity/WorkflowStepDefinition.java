package team5.prototype.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workflow_step_definitions")
@Getter
@Setter
public class WorkflowStepDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_duration_hours")
    private int defaultDurationHours;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "required_role_id", nullable = false)
    private Role requiredRole;

    @ManyToOne
    @JoinColumn(name = "workflow_definition_id")
    private WorkflowDefinition workflowDefinition;
}