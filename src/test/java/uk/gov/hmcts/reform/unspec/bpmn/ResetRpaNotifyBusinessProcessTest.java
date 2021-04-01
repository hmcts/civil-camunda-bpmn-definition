package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ResetRpaNotifyBusinessProcessTest extends BpmnBaseTest {

    public static final String RESET_RPA_NOTIFICATION_BUSINESS_PROCESS = "RESET_RPA_NOTIFICATION_BUSINESS_PROCESS";
    private static final String ACTIVITY_ID = "ResetRpaNotificationBusinessProcess";

    public ResetRpaNotifyBusinessProcessTest() {
        super(
            "reset_rpa_notify_business_process.bpmn",
            "RESET_RPA_NOTIFICATION_BUSINESS_PROCESS_ID"
        );
    }

    @Test
    void shouldSuccessfullyCompleteResetRpaNotifyBusinessProcess() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //complete the reset task
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);

        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            RESET_RPA_NOTIFICATION_BUSINESS_PROCESS,
            ACTIVITY_ID
        );
        assertNoExternalTasksLeft();
    }
}
