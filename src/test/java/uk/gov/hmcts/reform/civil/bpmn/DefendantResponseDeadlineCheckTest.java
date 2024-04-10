package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefendantResponseDeadlineCheckTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFENDANT_RESPONSE_DEADLINE_CHECK";
    public static final String PROCESS_ID = "DEFENDANT_RESPONSE_DEADLINE_CHECK";

    public DefendantResponseDeadlineCheckTest() {
        super("defendant_response_deadline_check.bpmn", "DEFENDANT_RESPONSE_DEADLINE_CHECK");
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyMultiparty() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowFlags", Map.of("DASHBOARD_SERVICE_ENABLED", true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification for claimant 1
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                                   PROCESS_CASE_EVENT,
                                   "CREATE_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE_DEADLINE_CLAIMANT",
                                   "GenerateClaimantDashboardNotificationDefendantResponseDeadlineCheck"
        );

        //complete the notification for defendant 1
        ExternalTask defendantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(defendantNotification,
                                   PROCESS_CASE_EVENT,
                                   "CREATE_DASHBOARD_NOTIFICATION_RESPONSE_DEADLINE_DEFENDANT",
                                   "GenerateDefendantDashboardNotificationRespondentResponseDeadlineCheck"
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
