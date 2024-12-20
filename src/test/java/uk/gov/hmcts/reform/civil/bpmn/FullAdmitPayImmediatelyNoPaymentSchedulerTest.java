package uk.gov.hmcts.reform.civil.bpmn;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.calendar.CronExpression;
import org.camunda.bpm.engine.management.JobDefinition;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

class FullAdmitPayImmediatelyNoPaymentSchedulerTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "FULL_ADMIT_PAY_IMMEDIATELY_NO_PAYMENT_CHECK";

    public FullAdmitPayImmediatelyNoPaymentSchedulerTest() {
        super("full_admit_pay_immediately_no_payment_scheduler.bpmn", "FULL_ADMIT_PAY_IMMEDIATELY_NO_PAYMENT_SCHEDULER");
    }

    @Test
    void schedulerShouldRaiseFullAdmitPayImmediatelyNoPaymentCheckExternalTask_whenStarted() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");

        String cronString = "0 0 0 * * ?";
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + cronString);
        assertCronTriggerFiresAtExpectedTime(
            new CronExpression(cronString),
            LocalDateTime.of(2024, 11, 30, 0, 0, 0),
            LocalDateTime.of(2024, 12, 1, 0, 0, 0)
        );

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        //fetch and complete task
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);

        assertThat(lockedExternalTasks).hasSize(1);
        completeTask(lockedExternalTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }
}
