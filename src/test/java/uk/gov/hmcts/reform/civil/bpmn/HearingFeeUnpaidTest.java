package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HearingFeeUnpaidTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "HEARING_FEE_UNPAID";
    public static final String PROCESS_ID = "HEARING_FEE_UNPAID";

    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String UNPAID_HEARING_FEE_NOTIFIER = "UnpaidHearingFeeNotifier";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID = "GenerateDashboardNotificationsHearingFeeUnpaid";

    public HearingFeeUnpaidTest() {
        super("hearing_fee_unpaid.bpmn", "HEARING_FEE_UNPAID");
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteHearingFeeUnpaidMultiparty(boolean twoRepresentatives) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
                TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
                UNREPRESENTED_DEFENDANT_ONE, false,
                DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the notification to first respondent
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                UNPAID_HEARING_FEE_NOTIFIER
        );

        //complete the notification to applicant
        ExternalTask defendantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(defendantDashboard,
                PROCESS_CASE_EVENT, DASHBOARD_NOTIFICATION_EVENT, CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteHearingFeeUnpaidOnePartyLip() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                UNREPRESENTED_DEFENDANT_ONE, true,
                DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the notification to first respondent
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                UNPAID_HEARING_FEE_NOTIFIER
        );

        //complete the notification to applicant
        ExternalTask defendantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(defendantDashboard,
                PROCESS_CASE_EVENT, DASHBOARD_NOTIFICATION_EVENT, CREATE_DASHBOARD_NOTIFICATION_FOR_HEARING_FEE_UNPAID
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
