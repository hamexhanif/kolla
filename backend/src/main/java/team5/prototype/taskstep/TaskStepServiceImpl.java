package team5.prototype.taskstep;

import org.springframework.stereotype.Service;

@Service
public class TaskStepServiceImpl implements TaskStepService{
    // TODO: implement assignTaskStepToUser, calculatePriority,
    //       setManualPriority, getTaskStepsByUserId, completeTaskStep

    @Override
    public void setManualPriority(Long taskStepId, int manualPriority) {
        // TODO: look up task step, set manualPriority, adjust priority if needed
    }
}
