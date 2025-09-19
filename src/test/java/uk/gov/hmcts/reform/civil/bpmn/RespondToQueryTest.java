package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RespondToQueryTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "queryManagementRespondQuery";
    public static final String PROCESS_ID = "queryManagementRespondQuery";
    private static final String NOTIFY_LR = "NOTIFY_RESPONSE_TO_QUERY";
    private static final String NOTIFY_OTHER_PARTY = "NOTIFY_OTHER_PARTY_QUERY_HAS_RESPONSE";
    private static final String NOTIFY_LR_ACTIVITY_ID = "QueryResponseNotify";
    private static final String NOTIFY_OTHER_PARTY_ACTIVITY_ID = "NotifyOtherPartyQueryHasResponse";
    private static final String CREATE_DASHBOARD_NOTIFICATION = "UPDATE_DASHBOARD_NOTIFICATIONS_RESPONSE_TO_QUERY";
    private static final String CREATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID = "UpdateDashboardNotificationsResponseToQuery";

    public RespondToQueryTest() {
        super("respond_to_query.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteRespondToQueryProcess_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the email notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_LR,
            NOTIFY_LR_ACTIVITY_ID
        );

        //complete the email notification
        ExternalTask notifyOtherParty = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyOtherParty,
            PROCESS_CASE_EVENT,
            NOTIFY_OTHER_PARTY,
            NOTIFY_OTHER_PARTY_ACTIVITY_ID
        );

        //create dashboard notification
        ExternalTask createDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyOtherParty,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION,
            CREATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID
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

        //fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
