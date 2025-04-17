package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RaiseQueryTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "queryManagementRaiseQuery";
    public static final String PROCESS_ID = "queryManagementRaiseQuery";
    private static final String GENERATE_QUERY_DOCUMENT = "GENERATE_QUERY_DOCUMENT";
    private static final String GENERATE_QUERY_DOCUMENT_ACTIVITY_ID = "GenerateQueryDocument";
    private static final String NOTIFY_LR = "NOTIFY_RAISED_QUERY";
    private static final String NOTIFY_OTHER_PARTY = "NOTIFY_OTHER_PARTY_FOR_RAISED_QUERY";
    private static final String NOTIFY_LR_ACTIVITY_ID = "QueryRaisedNotify";
    private static final String NOTIFY_OTHER_PARTY_ACTIVITY_ID = "NotifyOtherPartyQueryRaised";
    public static final String UPDATE_DASHBOARD_STATUS_ID = "UpdateDashboardNotificationsRaisedQm";

    public RaiseQueryTest() {
        super("raise_query.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteRaiseQueryProcess_whenCalled(boolean lipClaim) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, lipClaim));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (!lipClaim) {
            //generate the query document
            ExternalTask generateQueryDocumentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                generateQueryDocumentTask,
                PROCESS_CASE_EVENT,
                GENERATE_QUERY_DOCUMENT,
                GENERATE_QUERY_DOCUMENT_ACTIVITY_ID
            );
        }

        //complete the email notification
        ExternalTask notifyLrTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyLrTask,
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

        ExternalTask dashboardStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardStatus,
            PROCESS_CASE_EVENT,
            "UPDATE_DASHBOARD_NOTIFICATIONS_RAISED_QUERY",
            UPDATE_DASHBOARD_STATUS_ID
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
