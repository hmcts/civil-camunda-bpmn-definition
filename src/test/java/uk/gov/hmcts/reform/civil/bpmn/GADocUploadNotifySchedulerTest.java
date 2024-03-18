package uk.gov.hmcts.reform.civil.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.calendar.CronExpression;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

public class GADocUploadNotifySchedulerTest extends BpmnBaseTest {

    public static final String GA_DOC_UPLOAD_NOTIFY_SCHEDULER = "GADocUploadNotifyScheduler";

    public GADocUploadNotifySchedulerTest() {
        super("general_application_document_upload_notify_scheduler.bpmn", "GA_DOC_UPLOAD_NOTIFY_SCHEDULER");
    }

    @Test
    void pollingEventBmpShouldFirePollingEventEmitterExternalTask_whenStarted() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert first topic names
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GA_DOC_UPLOAD_NOTIFY_SCHEDULER);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");

        String cronString = "0 0 23 * * ?";
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + cronString);
        assertCronTriggerFiresAtExpectedTime(
                new CronExpression(cronString),
                LocalDateTime.of(2024, 1, 1, 23, 0, 0),
                LocalDateTime.of(2024, 1, 2, 23, 0, 0)
        );

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        //fetch and complete first task
        List<LockedExternalTask> lockedExternalGaResponseTasks = fetchAndLockTask(GA_DOC_UPLOAD_NOTIFY_SCHEDULER);
        assertThat(lockedExternalGaResponseTasks).hasSize(1);
        completeTask(lockedExternalGaResponseTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }
}
