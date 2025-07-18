package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UploadTranslatedDocumentGaJudgeDecisionTest extends BpmnBaseJudgeGASpecTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION";
    private static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOC_GA_DECISION_PROCESS_ID";
    //Add PDF document to main case
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddPDFDocumentToMainCase";

    private static final String CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION = "CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION";
    private static final String CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID
        = "makeDecisionCreateDashboardNotificationForApplicant";

    private static final String CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION = "CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION";
    private static final String CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID
        = "makeDecisionCreateDashboardNotificationForRespondent";

    private static final String BULK_PRINT_ORDER_APPLICANT = "SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT";
    private static final String BULK_PRINT_ORDER_APPLICANT_ACTIVITY_ID
        = "BulkPrintOrderApplicant";

    private static final String BULK_PRINT_ORDER_RESPONDENT = "SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT";
    private static final String BULK_PRINT_ORDER_RESPONDENT_ACTIVITY_ID
        = "BulkPrintOrderRespondent";

    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";
    private static final String APPLICATION_EVENT_GASPEC = "applicationEventGASpec";

    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";

    public UploadTranslatedDocumentGaJudgeDecisionTest() {
        super("upload_translated_document_ga_judge_decision.bpmn", "UPLOAD_TRANSLATED_DOC_GA_DECISION_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteJudgeDecisionNotifications_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, true,
            LIP_RESPONDENT, true
        ));

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
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

        //Complete Applicant Notification event
        ExternalTask judicialApplicantNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialApplicantNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_APPLICANT_NOTIFICATION_PROCESS_ID,
            variables
        );

        //Complete Respondent Notification event
        ExternalTask judicialRespondentNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialRespondentNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_RESPONDENT_NOTIFICATION_PROCESS_ID,
            variables
        );

        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION,
            CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID,
            variables
        );

        ExternalTask bulkPrintApplicantTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            bulkPrintApplicantTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            BULK_PRINT_ORDER_APPLICANT,
            BULK_PRINT_ORDER_APPLICANT_ACTIVITY_ID,
            variables
        );

        dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION,
            CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID,
            variables
        );

        ExternalTask bulkPrintRespondentTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            bulkPrintRespondentTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            BULK_PRINT_ORDER_RESPONDENT,
            BULK_PRINT_ORDER_RESPONDENT_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

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

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreatePDFDocumentForLRvLiP_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, false,
            LIP_RESPONDENT, true
        ));

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
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

        //Complete Applicant Notification event
        ExternalTask judicialApplicantNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialApplicantNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_APPLICANT_NOTIFICATION_PROCESS_ID,
            variables
        );

        //Complete Respondent Notification event
        ExternalTask judicialRespondentNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialRespondentNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_RESPONDENT_NOTIFICATION_PROCESS_ID,
            variables
        );

        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION,
            CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID,
            variables
        );

        ExternalTask bulkPrintRespondentTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            bulkPrintRespondentTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            BULK_PRINT_ORDER_RESPONDENT,
            BULK_PRINT_ORDER_RESPONDENT_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

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
