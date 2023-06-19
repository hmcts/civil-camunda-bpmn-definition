package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UnspecAutomatedHearingNoticeSchedulerTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "AUTOMATED_HEARING_NOTICE";

    public UnspecAutomatedHearingNoticeSchedulerTest() {
        super("unspec-automated-hearing-notice-scheduler.bpmn", "UnspecAutomatedHearingNoticeScheduler");
    }

    @Test
    void automatedHearingNoticeSchedulerSchedulerShouldFireAutomatedHearingNoticeExternalTask_whenStarted()
        throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(3);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");
        assertThat(jobDefinitions.get(1).getJobType()).isEqualTo("timer-intermediate-transition");
        assertThat(jobDefinitions.get(2).getJobType()).isEqualTo("timer-transition");

        String cronString = "0 0 0,12 ? * * *";
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + cronString);
        assertThat(jobDefinitions.get(1).getJobConfiguration()).isEqualTo("DURATION: PT30S");
        assertThat(jobDefinitions.get(2).getJobConfiguration()).isEqualTo("DURATION: PT30M");

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
