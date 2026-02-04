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

class GenerateSpecDjFormTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_SPEC";
    public static final String PROCESS_ID = "GENERATE_DJ_FORM_SPEC";

    public static final String GENERATE_DJ_FORM_SPEC_EVENT = "GENERATE_DJ_FORM_SPEC";
    public static final String GENERATE_DJ_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormSpec";
    public static final String NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED = "NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED";
    public static final String NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED_ACTIVITY_ID = "NotifyApplicantSolicitorDJReceived";
    public static final String NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED = "NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED";
    public static final String NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED_ACTIVITY_ID = "NotifyRespondentSolicitorDJReceived";
    public static final String NOTIFY_RPA_DJ_SPEC = "NOTIFY_RPA_DJ_SPEC";
    public static final String NOTIFY_RPA_DJ_SPEC_ACTIVITY_ID = "NotifyRPADJSPEC";
    private static final String DASHBOARD_NOTIFICATION_ACTIVITY_ID = "GenerateDashboardNotificationsDjFormSpec";

    public GenerateSpecDjFormTest() {
        super("generate_spec_DJ_form.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteWhenJoEnabled(boolean dashboardServiceEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            JO_ONLINE_LIVE_ENABLED, true,
            DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled,
            LIP_CASE, false,
            UNREPRESENTED_DEFENDANT_ONE, false
        ));

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask generateDJFormSpecTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDJFormSpecTask,
            PROCESS_CASE_EVENT,
            GENERATE_DJ_FORM_SPEC_EVENT,
            GENERATE_DJ_FORM_SPEC_ACTIVITY_ID,
            variables
        );

        ExternalTask notifyApplicantTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicantTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED,
            NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED_ACTIVITY_ID,
            variables
        );

        ExternalTask notifyRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED,
            NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED_ACTIVITY_ID,
            variables
        );

        ExternalTask notifyRpaTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRpaTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_DJ_SPEC,
            NOTIFY_RPA_DJ_SPEC_ACTIVITY_ID,
            variables
        );

        if (dashboardServiceEnabled) {
            ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardNotificationTask,
                PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                DASHBOARD_NOTIFICATION_ACTIVITY_ID,
                variables
            );
        }

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteLipVlipWhenJoDisabled(boolean dashboardServiceEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            JO_ONLINE_LIVE_ENABLED, false,
            DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled,
            LIP_CASE, true,
            UNREPRESENTED_DEFENDANT_ONE, true
        ));

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notifyApplicantTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicantTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED,
            NOTIFY_APPLICANT_SOLICITOR_DJ_RECEIVED_ACTIVITY_ID,
            variables
        );

        ExternalTask notifyRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED,
            NOTIFY_RESPONDENT_SOLICITOR_DJ_RECEIVED_ACTIVITY_ID,
            variables
        );

        ExternalTask notifyRpaTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRpaTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_DJ_SPEC,
            NOTIFY_RPA_DJ_SPEC_ACTIVITY_ID,
            variables
        );

        if (dashboardServiceEnabled) {
            ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardNotificationTask,
                PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                DASHBOARD_NOTIFICATION_ACTIVITY_ID,
                variables
            );
        }

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

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
