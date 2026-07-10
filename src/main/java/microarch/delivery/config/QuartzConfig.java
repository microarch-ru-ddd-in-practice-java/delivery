package microarch.delivery.config;

import microarch.delivery.adapters.in.jobs.AssignOrderJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail assignOrderJobDetail() {
        return JobBuilder.newJob(AssignOrderJob.class)
                .withIdentity("assignOrderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger assignOrderTrigger(JobDetail assignOrderJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(assignOrderJobDetail)
                .withIdentity("assignOrderTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever())
                .build();
    }
}
