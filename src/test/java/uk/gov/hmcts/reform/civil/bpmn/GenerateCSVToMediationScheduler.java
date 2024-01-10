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

public class GenerateCSVToMediationScheduler extends BpmnBaseTest {

    public static final String GENERATE_CSV_SCHEDULER = "GenerateCsvAndSendToMmt";

    public GenerateCSVToMediationScheduler() {
        super("in_mediation_file_transfer_scheduler_mmt.bpmn", "GenerateCsvAndSendToMmtScheduler");
    }

    @Test
    void shouldSuccessfullyCompleteEVent_whenCalled() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert first topic names
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GENERATE_CSV_SCHEDULER);

        //get jobs
        List<JobDefinition> jobDefinitions = getJobs();

        //assert that job is as expected
        assertThat(jobDefinitions).hasSize(1);
        assertThat(jobDefinitions.get(0).getJobType()).isEqualTo("timer-start-event");

        String cronString = "0 0 1 ? * * *";
        assertThat(jobDefinitions.get(0).getJobConfiguration()).isEqualTo("CYCLE: " + cronString);
        assertCronTriggerFiresAtExpectedTime(
            new CronExpression(cronString),
            LocalDateTime.of(2020, 1, 1, 1, 0, 0),
            LocalDateTime.of(2020, 1, 2, 1, 0, 0)
        );

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        //fetch and complete first task
        List<LockedExternalTask> lockedExternalGaResponseTasks = fetchAndLockTask(GENERATE_CSV_SCHEDULER);
        assertThat(lockedExternalGaResponseTasks).hasSize(1);
        completeTask(lockedExternalGaResponseTasks.get(0).getId());

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }
}
