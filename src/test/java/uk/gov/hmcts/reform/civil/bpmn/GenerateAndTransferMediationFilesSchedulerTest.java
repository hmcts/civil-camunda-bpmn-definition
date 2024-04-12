package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.calendar.CronExpression;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GenerateAndTransferMediationFilesSchedulerTest extends BpmnBaseTest {

    public static final String GENERATE_CSV_SCHEDULER_TOPIC = "GenerateCsvAndSendToMmt";
    public static final String GENERATE_JSON_SCHEDULER_TOPIC = "GenerateJsonAndSendToMmt";

    public GenerateAndTransferMediationFilesSchedulerTest() {
        super("in_mediation_file_transfer_scheduler_mmt.bpmn", "GenerateCsvAndSendToMmtScheduler");
    }

    @Test
    void shouldSuccessfullyCompleteCSVEvent_whenCalledCarmToggleOff() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert first topic names
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GENERATE_CSV_SCHEDULER_TOPIC);

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
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(GENERATE_CSV_SCHEDULER_TOPIC);
        assertThat(lockedExternalTasks).hasSize(1);

        VariableMap variables = Variables.createVariables();
        variables.putValue("carmFeatureEnabled", false);
        completeTask(lockedExternalTasks.get(0).getId(), variables);

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }

    @Test
    void shouldSuccessfullyCompleteCSVAndJsonEvents_whenCalledCarmToggleOn() throws ParseException {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert first topic names
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GENERATE_CSV_SCHEDULER_TOPIC);

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
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(GENERATE_CSV_SCHEDULER_TOPIC);
        assertThat(lockedExternalTasks).hasSize(1);

        VariableMap variables = Variables.createVariables();
        variables.putValue("carmFeatureEnabled", true);
        completeTask(lockedExternalTasks.get(0).getId(), variables);

        //assert second task
        List<ExternalTask> externalTasksAfterCsvCarmEnabled = getExternalTasks();
        assertThat(externalTasksAfterCsvCarmEnabled).hasSize(1);

        //assert second topic name
        assertThat(getTopics()).hasSize(1);
        assertThat(getTopics()).containsOnly(GENERATE_JSON_SCHEDULER_TOPIC);

        List<LockedExternalTask> lockedExternalTasksSecond = fetchAndLockTask(GENERATE_JSON_SCHEDULER_TOPIC);
        assertThat(lockedExternalTasksSecond).hasSize(1);

        completeTask(lockedExternalTasksSecond.get(0).getId(), variables);

        //assert no external tasks left
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();

        //assert process is still active - timer event so always running
        assertFalse(processInstance.isEnded());
    }
}
