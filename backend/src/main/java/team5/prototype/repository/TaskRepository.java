package team5.prototype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team5.prototype.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Vorerst leer, da JpaRepository alles Nötige bereitstellt.
    // Wichtig: Wir verwenden hier auch "Long", da die Task-ID wahrscheinlich auch ein Long sein wird.
}