package team5.prototype.taskstep;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStepDto {
    private Long id;
    private String name;
    private String status;
    private String assignedUsername; // Besserer Name als nur 'assignedUser'
    private String priority;
}