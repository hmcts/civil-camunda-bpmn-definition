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

class SettlementNoResponseFromDefendantSchedulerTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "SETTLEMENT_NO_RESPONSE_FROM_DEFENDANT_CHECK";

    public SettlementNoResponseFromDefendantSchedulerTest() {
        super("settlement_no_defendant_response_scheduler.bpmn", "SETTLEMENT_NO_RESPONSE_FROM_DEFENDANT_SCHEDULER");
    }

    @Test
    void schedulerShouldRaiseSettlementNoResponseFromDefendantCheckExternalTask_whenStarted() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");

        String cronString = "0 0 1 * * ? 2026";
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + cronString);
        assertCronTriggerFiresAtExpectedTime(
            new CronExpression(cronString),
            LocalDateTime.of(2024, 11, 30, 1, 0, 0),
            LocalDateTime.of(2026, 1, 1, 1, 0, 0)
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
