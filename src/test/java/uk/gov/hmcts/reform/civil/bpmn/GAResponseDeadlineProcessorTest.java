package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.calendar.CronExpression;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GAResponseDeadlineProcessorTest extends BpmnBaseTest {

    public static final String GA_RESPONSE_DEADLINE_TOPIC = "GAResponseDeadlineProcessor";
    public static final String GA_JUDGE_REVISIT_TOPIC = "GAJudgeRevisitProcessor";

    public GAResponseDeadlineProcessorTest() {
        super("general_application_response_deadline_processor.bpmn", "GA_RESPONSE_DEADLINE_PROCESSOR");
    }

    @Test
    void pollingEventBmpnShouldFirePollingEventEmmiterExternalTask_whenStarted() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert first topic names
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GA_RESPONSE_DEADLINE_TOPIC);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");

        String cronString = "0 15 17 ? * * *";
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + cronString);
        assertCronTriggerFiresAtExpectedTime(
                new CronExpression(cronString),
                LocalDateTime.of(2020, 1, 1, 17, 15, 0),
                LocalDateTime.of(2020, 1, 2, 17, 15, 0)
        );

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        //fetch and complete first task
        List<LockedExternalTask> lockedExternalGaRessponseTasks = fetchAndLockTask(GA_RESPONSE_DEADLINE_TOPIC);
        assertThat(lockedExternalGaRessponseTasks).hasSize(1);
        completeTask(lockedExternalGaRessponseTasks.get(0).getId());

        //assert second topic names
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GA_JUDGE_REVISIT_TOPIC);

        //fetch and complete second task
        List<LockedExternalTask> lockedExternalGaJudgeTasks = fetchAndLockTask(GA_JUDGE_REVISIT_TOPIC);
        assertThat(lockedExternalGaJudgeTasks).hasSize(1);
        completeTask(lockedExternalGaJudgeTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }
}
