package uk.gov.hmcts.reform.civil.bpmn;

import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InitiateGeneralApplicationAfterPaymentTest extends BpmnBaseGAAfterPaymentTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT";
    private static final String PROCESS_ID = "GA_INITIATE_AFTER_PAYMENT_PROCESS_ID";
    public static final String APPLICATION_PROCESS_CASE_EVENT = "applicationProcessCaseEventGASpec";
    private static final String GENERATE_DRAFT_DOCUMENT = "GENERATE_DRAFT_DOCUMENT";
    private static final String GENERATE_DRAFT_DOCUMENT_ID = "GenerateDraftDocumentId";
    public static final String UPDATE_FROM_GA_CASE_EVENT = "updateFromGACaseEvent";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDraftDocToMainCaseID";

    //Notifying respondents
    public static final String PROCESS_EXTERNAL_CASE_EVENT = "processExternalCaseEventGASpec";
    private static final String NOTYFYING_RESPONDENTS_EVENT = "NOTIFY_GENERAL_APPLICATION_RESPONDENT";
    private static final String GENERAL_APPLICATION_NOTIYFYING_ID = "GeneralApplicationNotifying";
    private static final String ASSIGNIN_OF_ROLES_EVENT = "ASSIGN_GA_ROLES";
    private static final String ASSIGNIN_OF_ROLES_ID = "AssigningOfRoles";
    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";
    private static final String CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_EVENT = "CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT";
    private static final String CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_ACTIVITY_ID = "respondentApplicationSubmittedDashboardNotification";
    public static final String APPLICATION_EVENT = "applicationEventGASpec";
    private static final String UPDATE_CLAIMANT_GA_TASK_LIST_GA_COMPLETE_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA_COMPLETE";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ACTIVITY_ID = "GeneralApplicationClaimantTaskList";
    private static final String UPDATE_RESPONDENT_GA_TASK_LIST_GA_COMPLETE_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA_COMPLETE";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ACTIVITY_ID = "GeneralApplicationRespondentTaskList";

    public InitiateGeneralApplicationAfterPaymentTest() {
        super("initiate_general_application_after_payment.bpmn",
              "GA_INITIATE_AFTER_PAYMENT_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteCreateGeneralApplication_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, false,
            LIP_RESPONDENT, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //assigne of roles
        ExternalTask assignRoles = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignRoles,
            APPLICATION_PROCESS_CASE_EVENT,
            ASSIGNIN_OF_ROLES_EVENT,
            ASSIGNIN_OF_ROLES_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_DRAFT_DOCUMENT,
            GENERATE_DRAFT_DOCUMENT_ID,
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
            NOTYFYING_RESPONDENTS_EVENT,
            GENERAL_APPLICATION_NOTIYFYING_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreateGeneralApplicationForLiPvLiP_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, false,
            LIP_RESPONDENT, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //assigne of roles
        ExternalTask assignRoles = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignRoles,
            APPLICATION_PROCESS_CASE_EVENT,
            ASSIGNIN_OF_ROLES_EVENT,
            ASSIGNIN_OF_ROLES_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_DRAFT_DOCUMENT,
            GENERATE_DRAFT_DOCUMENT_ID,
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
            NOTYFYING_RESPONDENTS_EVENT,
            GENERAL_APPLICATION_NOTIYFYING_ID,
            variables
        );

       //create dashboard notification
       ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotification,
            PROCESS_EXTERNAL_CASE_EVENT,
            CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_EVENT,
            CREATE_APPLICATION_SUBMITTED_DASHBOARD_NOTIFICATION_FOR_RESPONDENT_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        ExternalTask claimantTaskListUpdate = assertNextExternalTask(APPLICATION_EVENT);
        assertCompleteExternalTask(
            claimantTaskListUpdate,
            APPLICATION_EVENT,
            UPDATE_CLAIMANT_GA_TASK_LIST_GA_COMPLETE_EVENT,
            GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ACTIVITY_ID,
            variables
        );

        ExternalTask respondentTaskListUpdate = assertNextExternalTask(APPLICATION_EVENT);
        assertCompleteExternalTask(
            respondentTaskListUpdate,
            APPLICATION_EVENT,
            UPDATE_RESPONDENT_GA_TASK_LIST_GA_COMPLETE_EVENT,
            GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ACTIVITY_ID,
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
