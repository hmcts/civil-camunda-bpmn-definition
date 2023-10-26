package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HandleHmcMessageTest extends BpmnBaseTest {

    public static final String TOPIC_NAME = "HANDLE_HMC_MESSAGE_TASK";
    public static final String MESSAGE_NAME = "HANDLE_HMC_MESSAGE";
    public static final String PROCESS_ID = "HANDLE_HMC_MESSAGE_ID";

    public HandleHmcMessageTest() {
        super("handle_hmc_message.bpmn", "HANDLE_HMC_MESSAGE_ID");
    }

    @Test
    void shouldFireHandleHMCMessageExternalTask_whenStarted() {
        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert topic names
        assertThat(getTopics()).containsOnly(TOPIC_NAME);

        //get external tasks
        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        //fetch and complete task
        List<LockedExternalTask> lockedExternalTasks = fetchAndLockTask(TOPIC_NAME);

        assertThat(lockedExternalTasks).hasSize(1);
        completeTask(lockedExternalTasks.get(0).getId());

        //assert no external tasks left
        assertNoExternalTasksLeft();
    }
}
