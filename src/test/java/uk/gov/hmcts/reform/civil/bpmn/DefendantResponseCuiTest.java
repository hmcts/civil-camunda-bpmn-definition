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
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CONTACT_DETAILS_CHANGE
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CONTACT_DETAILS_CHANGE";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI";
    private static final String NOTIFY_LIP_DEFENDANT_FOR_RESPONSE_SUBMISSION
        = "NOTIFY_LIP_DEFENDANT_RESPONSE_SUBMISSION";
    private static final String GENERATE_LIP_RESPONSE_PDF = "GENERATE_RESPONSE_CUI_SEALED";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CONTACT_CHANGE_ACTIVITY_ID
        = "DefendantContactDetailsChangeNotifyApplicantSolicitor1";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_ACTIVITY_ID
        = "DefendantResponseNotifyApplicantSolicitor1ForCui";
    private static final String NOTIFY_LIP_DEFENDANT_FOR_RESPONSE_SUBMISSION_ACTIVITY_ID
        = "DefendantLipResponseNotifyDefendant";
    private static final String GENERATE_LIP_RESPONSE_PDF_ACTIVITY = "GenerateSealedLipResponsePdf";

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
            "RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL", false));

        assertBusinessProcessHasStarted(variables);

        verifyApplicantNotificationOfAddressChangeCompleted();
        verifyDefendantLipNotificationOfResponseSubmissionCompleted();
        verifyApplicantNotificationOfResponseSubmissionCompleted();
        verifySealedResponseGenerationCompleted();

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSkipNotifyApplicantSolicitor_whenNoContactDetailsChange() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "CONTACT_DETAILS_CHANGE", false));

        assertBusinessProcessHasStarted(variables);
        verifyDefendantLipNotificationOfResponseSubmissionCompleted();
        verifyApplicantNotificationOfResponseSubmissionCompleted();
        verifySealedResponseGenerationCompleted();

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotNotifyOrGeneratePdf_whenDefendantResponseBilingual() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL", true));

        assertBusinessProcessHasStarted(variables);
        verifyDefendantLipNotificationOfResponseSubmissionCompleted();

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void verifyApplicantNotificationOfAddressChangeCompleted() {
        verifyTaskIsComplete(
            NOTIFY_RESPONDENT_SOLICITOR_1_CONTACT_DETAILS_CHANGE,
            NOTIFY_RESPONDENT_SOLICITOR_1_CONTACT_CHANGE_ACTIVITY_ID
        );
    }

    private void verifyApplicantNotificationOfResponseSubmissionCompleted() {
        verifyTaskIsComplete(
            NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CUI,
            NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_ACTIVITY_ID
        );
    }

    private void verifyDefendantLipNotificationOfResponseSubmissionCompleted() {
        verifyTaskIsComplete(
            NOTIFY_LIP_DEFENDANT_FOR_RESPONSE_SUBMISSION,
            NOTIFY_LIP_DEFENDANT_FOR_RESPONSE_SUBMISSION_ACTIVITY_ID
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

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

}
