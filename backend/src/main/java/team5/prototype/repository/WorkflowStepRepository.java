package team5.prototype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team5.prototype.entity.WorkflowStep;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    // Vorerst leer.
}