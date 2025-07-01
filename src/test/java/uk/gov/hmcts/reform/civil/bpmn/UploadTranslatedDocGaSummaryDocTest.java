package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseGASpecTest.START_GA_BUSINESS_ACTIVITY;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseGASpecTest.START_GA_BUSINESS_EVENT;

public class UploadTranslatedDocGaSummaryDocTest extends BpmnBaseGAAfterPaymentTest {
    public static final String END_BUSINESS_PROCESS = "END_BUSINESS_PROCESS_GASPEC";
    private static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    private static final String WELSH_ENABLED = "WELSH_ENABLED";
    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";
    public static final String UPDATE_FROM_GA_CASE_EVENT = "updateFromGACaseEvent";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDraftDocToMainCaseID";
    public static final String PROCESS_EXTERNAL_CASE_EVENT = "processExternalCaseEventGASpec";
    private static final String NOTIFYING_RESPONDENTS_EVENT = "NOTIFY_GENERAL_APPLICATION_RESPONDENT";
    private static final String GENERAL_APPLICATION_NOTIFYING_ID = "GeneralApplicationNotifying";
    public static final String APPLICATION_PROCESS_EVENT_GA_SPEC = "applicationProcessCaseEventGASpec";
    private static final String UPDATE_DASHBOARD_NOTIFICATIONS = "UPDATE_GA_DASHBOARD_NOTIFICATION";
    private static final String CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_EVENT = "CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT";
    private static final String CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_ACTIVITY_ID = "respondentDashboardNotification";

    private static final String UPDATE_DASHBOARD_NOTIFICATIONS_ID = "UpdateDashboardNotifications";
    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";

    private static final String APPLICATION_EVENT_GASPEC = "applicationEventGASpec";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";
    public static final String NOTIFY_EVENT = "processExternalCaseEventGASpec";

    public UploadTranslatedDocGaSummaryDocTest() {
        super("upload_translated_document_ga_summary_doc.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_DOC");
    }


    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "true,true", "false,true"})
    void shouldResumePausedTaskAfterUploadingTranslatedDocument(boolean isLipApplicant, boolean isLipRespondent) {
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            WELSH_ENABLED, "true",
            LIP_APPLICANT, isLipApplicant,
            LIP_RESPONDENT , isLipRespondent));

        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_GA_BUSINESS_EVENT,
            START_GA_BUSINESS_ACTIVITY,
            variables
        );

        //Complete add pdf to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //notify respondents
        ExternalTask notifyRespondents = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondents,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFYING_RESPONDENTS_EVENT,
            GENERAL_APPLICATION_NOTIFYING_ID,
            variables
        );

        if (isLipApplicant) {
            //update dashboard
            ExternalTask updateCuiClaimantDashboard = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GA_SPEC);
            assertCompleteExternalTask(
                updateCuiClaimantDashboard,
                APPLICATION_PROCESS_EVENT_GA_SPEC,
                UPDATE_DASHBOARD_NOTIFICATIONS,
                UPDATE_DASHBOARD_NOTIFICATIONS_ID,
                variables
            );

            //post translated document to LiP applicant
            ExternalTask bulkPrintApplicantTask = assertNextExternalTask(NOTIFY_EVENT);
            assertCompleteExternalTask(
                bulkPrintApplicantTask,
                NOTIFY_EVENT,
                "SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT",
                "BulkPrintOrderApplicant",
                variables
            );
        }

        if (isLipRespondent) {
            //update dashboard
            ExternalTask updateCuiRespondentDashboard = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GA_SPEC);
            assertCompleteExternalTask(
                updateCuiRespondentDashboard,
                APPLICATION_PROCESS_EVENT_GA_SPEC,
                CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_EVENT,
                CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_ACTIVITY_ID,
                variables
            );

            ExternalTask bulkPrintRespondentTask = assertNextExternalTask(NOTIFY_EVENT);
            assertCompleteExternalTask(
                bulkPrintRespondentTask,
                NOTIFY_EVENT,
                "SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT",
                "BulkPrintOrderRespondent",
                variables
            );
        }

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        if (isLipApplicant || isLipRespondent) {
            //update dashboard
            ExternalTask updateCuiClaimantDashboard = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
            assertCompleteExternalTask(
                updateCuiClaimantDashboard,
                APPLICATION_EVENT_GASPEC,
                UPDATE_CLAIMANT_DASHBOARD_GA_EVENT,
                GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID,
                variables
            );

            ExternalTask updateCuiDefendantDashboard = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
            assertCompleteExternalTask(
                updateCuiDefendantDashboard,
                APPLICATION_EVENT_GASPEC,
                UPDATE_RESPONDENT_DASHBOARD_GA_EVENT,
                GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID,
                variables
            );
        }

        assertNoExternalTasksLeft();
    }

}
