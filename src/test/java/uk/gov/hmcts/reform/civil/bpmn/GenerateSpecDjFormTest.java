package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateSpecDjFormTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_SPEC";
    private static final String PROCESS_ID = "GENERATE_DJ_FORM_SPEC";

    private static final String GENERATE_DJ_FORM_SPEC_EVENT = "GENERATE_DJ_FORM_SPEC";
    private static final String GENERATE_DJ_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormSpec";
    private static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    private static final String NOTIFY_EVENT_ACTIVITY_ID = "GenerateSpecDjFormNotifier";
    private static final String NOTIFY_RPA_EVENT = "NOTIFY_RPA_DJ_SPEC";
    private static final String NOTIFY_RPA_ACTIVITY_ID = "NotifyRPADJSPEC";
    private static final String DASHBOARD_APPLICATION_OFFLINE_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT";
    private static final String DASHBOARD_APPLICATION_OFFLINE_CLAIMANT_ACTIVITY_ID = "claimantLipApplicationOfflineDashboardNotification";
    private static final String DASHBOARD_APPLICATION_OFFLINE_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT";
    private static final String DASHBOARD_APPLICATION_OFFLINE_DEFENDANT_ACTIVITY_ID = "defendantLipApplicationOfflineDashboardNotification";
    private static final String DASHBOARD_CCJ_APPLICANT = "CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1";
    private static final String DASHBOARD_CCJ_APPLICANT_ACTIVITY_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForApplicant1";
    private static final String DASHBOARD_CCJ_RESPONDENT = "CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1";
    private static final String DASHBOARD_CCJ_RESPONDENT_ACTIVITY_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForRespondent1_1";

    GenerateSpecDjFormTest() {
        super("generate_spec_DJ_form.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyComplete_whenJoFlagEnabledAndDashboardDisabled() {
        VariableMap variables = flowFlags(true, false, false, false);

        startAndAssertProcess();
        startBusinessProcess(variables);
        completeDocGeneration(variables);
        completePrimaryNotifications(variables);
        assertProcessClosed();
    }

    @Test
    void shouldSuccessfullyComplete_whenDashboardEnabledAndClaimantRepresented() {
        VariableMap variables = flowFlags(false, true, false, false);

        startAndAssertProcess();
        startBusinessProcess(variables);
        completeDocGeneration(variables);
        completePrimaryNotifications(variables);

        ExternalTask defendantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantDashboard,
            PROCESS_CASE_EVENT,
            DASHBOARD_APPLICATION_OFFLINE_DEFENDANT,
            DASHBOARD_APPLICATION_OFFLINE_DEFENDANT_ACTIVITY_ID,
            variables
        );

        ExternalTask respondentDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentDashboard,
            PROCESS_CASE_EVENT,
            DASHBOARD_CCJ_RESPONDENT,
            DASHBOARD_CCJ_RESPONDENT_ACTIVITY_ID,
            variables
        );

        assertProcessClosed();
    }

    @Test
    void shouldSuccessfullyComplete_whenLipVLipAndDashboardEnabled() {
        VariableMap variables = flowFlags(false, true, true, true);

        startAndAssertProcess();
        startBusinessProcess(variables);
        completePrimaryNotifications(variables);

        ExternalTask claimantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDashboard,
            PROCESS_CASE_EVENT,
            DASHBOARD_APPLICATION_OFFLINE_CLAIMANT,
            DASHBOARD_APPLICATION_OFFLINE_CLAIMANT_ACTIVITY_ID,
            variables
        );

        ExternalTask applicantCcjNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantCcjNotification,
            PROCESS_CASE_EVENT,
            DASHBOARD_CCJ_APPLICANT,
            DASHBOARD_CCJ_APPLICANT_ACTIVITY_ID,
            variables
        );

        ExternalTask defendantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantDashboard,
            PROCESS_CASE_EVENT,
            DASHBOARD_APPLICATION_OFFLINE_DEFENDANT,
            DASHBOARD_APPLICATION_OFFLINE_DEFENDANT_ACTIVITY_ID,
            variables
        );

        ExternalTask respondentDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentDashboard,
            PROCESS_CASE_EVENT,
            DASHBOARD_CCJ_RESPONDENT,
            DASHBOARD_CCJ_RESPONDENT_ACTIVITY_ID,
            variables
        );

        assertProcessClosed();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        startAndAssertProcess();

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }

    private void completePrimaryNotifications(VariableMap variables) {
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notification,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            NOTIFY_EVENT_ACTIVITY_ID,
            variables
        );

        ExternalTask roboticsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            roboticsNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_EVENT,
            NOTIFY_RPA_ACTIVITY_ID,
            variables
        );
    }

    private void completeDocGeneration(VariableMap variables) {
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_DJ_FORM_SPEC_EVENT,
            GENERATE_DJ_FORM_SPEC_ACTIVITY_ID,
            variables
        );
    }

    private void startAndAssertProcess() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
    }

    private void assertProcessClosed() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    private VariableMap flowFlags(boolean joLiveEnabled,
                                  boolean dashboardEnabled,
                                  boolean lipCase,
                                  boolean unrepresentedDefendantOne) {
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            JO_ONLINE_LIVE_ENABLED, joLiveEnabled,
            DASHBOARD_SERVICE_ENABLED, dashboardEnabled,
            LIP_CASE, lipCase,
            UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendantOne
        ));
        return variables;
    }
}
