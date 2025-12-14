package team5.prototype.user;

import org.springframework.stereotype.Service;
import team5.prototype.task.Task;
import team5.prototype.task.TaskRepository; // Sie werden dies wahrscheinlich auch benötigen

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public UserServiceImpl(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getTasksForUser(String userId) {
        // TODO: Später die Logik implementieren, um die Tasks eines Benutzers
        // aus dem TaskRepository zu laden (z.B. über eine benutzerdefinierte Abfrage).
        System.out.println("Rufe Tasks für Benutzer " + userId + " ab.");
        return Collections.emptyList(); // Gibt vorerst eine leere Liste zurück
    }
}