package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RetryRpaNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    public static final String PROCESS_ID = "RETRY_RPA_NOTIFICATION_PROCESS_ID";

    public static final String RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String ACTIVITY_ID = "RetryRoboticsNotification";

    public RetryRpaNotificationTest() {
        super("retry_rpa_notification.bpmn", "RETRY_RPA_NOTIFICATION_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteRetryRpaNotification() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   RETRY_NOTIFY_RPA_ON_CASE_HANDED_OFFLINE, ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();

        //fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
