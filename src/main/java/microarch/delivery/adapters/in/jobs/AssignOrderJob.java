package microarch.delivery.adapters.in.jobs;

import microarch.delivery.core.application.commands.AssignOrderCommand;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class AssignOrderJob implements Job {

    private final AssignOrderCommandHandler commandHandler;

    public AssignOrderJob(AssignOrderCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void execute(JobExecutionContext context) {
        commandHandler.handle(new AssignOrderCommand());
    }
}
