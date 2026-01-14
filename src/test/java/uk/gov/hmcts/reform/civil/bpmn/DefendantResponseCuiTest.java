package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DefendantResponseCuiTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "DEFENDANT_RESPONSE_CUI";
    private static final String PROCESS_ID = "DEFENDANT_RESPONSE_PROCESS_ID_CUI";

    //CCD Case Event
    private static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    private static final String GENERATE_RESPONSE_DQ_LIP_SEALED_PDF = "GENERATE_RESPONSE_DQ_LIP_SEALED";
    private static final String GENERATE_LIP_RESPONSE_PDF = "GENERATE_RESPONSE_CUI_SEALED";

    //ACTIVITY IDs
    private static final String NOTIFY_EVENT_ACTIVITY = "DefendantResponseCUINotify";
    private static final String GENERATE_LIP_DQ_PDF_ACTIVITY = "GenerateSealedLipDQPdf";
    private static final String GENERATE_LIP_RESPONSE_PDF_ACTIVITY = "GenerateSealedLipResponsePdf";
    private static final String GENERATE_DASHBOARD_ACTIVITY
        = "GenerateDashboardNotificationsDefendantResponse";

    public DefendantResponseCuiTest() {
        super(
            "defendant_response_cui.bpmn",
            "DEFENDANT_RESPONSE_PROCESS_ID_CUI"
        );
    }

    @Test
    void shouldCompleteTheProcessWithNotificationsAndPdfGeneration_whenNotBilingualAndContactsChanged() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "CONTACT_DETAILS_CHANGE", true,
            "RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL", false,
            "CLAIM_ISSUE_BILINGUAL", true,
            "DASHBOARD_SERVICE_ENABLED", true));

        assertBusinessProcessHasStarted(variables);

        verifyNotifyPartiesCompleted();
        verifyGenerateDashboardNotifications();
        verifySealedDQGenerationCompleted();
        verifySealedResponseGenerationCompleted();

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotCompleteTheProcessWithNotificationsAndPdfGeneration_whenNotBilingualButClaimantBilingual_whenEnglishToWelshEnabled() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "CONTACT_DETAILS_CHANGE", true,
            "RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL", false,
            "CLAIM_ISSUE_BILINGUAL", true,
            "WELSH_ENABLED", true,
            "DASHBOARD_SERVICE_ENABLED", true));

        assertBusinessProcessHasStarted(variables);

        verifyGenerateDashboardNotifications();
        verifySealedDQGenerationCompleted();
        verifySealedResponseGenerationCompleted();

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotSendDashboardNotifications_whenDashboardServiceDisabled() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "CLAIM_ISSUE_BILINGUAL", true,
            "RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL", true));

        assertBusinessProcessHasStarted(variables);
        verifyGenerateDashboardNotifications();
        verifySealedDQGenerationCompleted();
        verifySealedResponseGenerationCompleted();

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void verifyNotifyPartiesCompleted() {
        verifyTaskIsComplete(NOTIFY_EVENT, NOTIFY_EVENT_ACTIVITY);
    }

    private void verifySealedDQGenerationCompleted() {
        verifyTaskIsComplete(
            GENERATE_RESPONSE_DQ_LIP_SEALED_PDF,
            GENERATE_LIP_DQ_PDF_ACTIVITY
        );
    }

    private void verifySealedResponseGenerationCompleted() {
        verifyTaskIsComplete(
            GENERATE_LIP_RESPONSE_PDF,
            GENERATE_LIP_RESPONSE_PDF_ACTIVITY
        );
    }

    private void verifyTaskIsComplete(String caseEvent, String actionId) {
        ExternalTask externalTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            externalTask,
            PROCESS_CASE_EVENT,
            caseEvent,
            actionId
        );
    }

    private void assertBusinessProcessHasStarted(VariableMap variables) {
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
    }

    private void verifyGenerateDashboardNotifications() {
        verifyTaskIsComplete(
            DASHBOARD_NOTIFICATION_EVENT,
            GENERATE_DASHBOARD_ACTIVITY
        );
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

}
