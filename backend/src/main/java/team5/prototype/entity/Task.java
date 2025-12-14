package team5.prototype.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private ZonedDateTime deadline;

    @Transient // Wird nicht in der DB gespeichert, nur zur Laufzeit berechnet
    private Priority overriddenPriority;

    @ManyToOne
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<WorkflowStep> steps;
}
