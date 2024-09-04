package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JudgeMakesDecisionGeneralApplicationTest extends BpmnBaseJudgeGASpecTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "MAKE_DECISION";
    private static final String PROCESS_ID = "GA_MAKE_DECISION_PROCESS_ID";
    //Create PDF
    private static final String CREATE_PDF_EVENT = "GENERATE_JUDGES_FORM";
    private static final String CREATE_PDF_ID = "CreatePDFDocument";
    //Add PDF document to main case
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddPDFDocumentToMainCase";

    //Obtain Additional fee value
    private static final String OBTAIN_ADDITIONAL_FEE_VALUE_EVENT = "OBTAIN_ADDITIONAL_FEE_VALUE";
    private static final String OBTAIN_ADDITIONAL_FEE_VALUE_ID = "ObtainAdditionalFeeValue";
    //Create PDF
    private static final String OBTAIN_ADDIIONAL_FEE_REFERENCE_EVENT = "OBTAIN_ADDITIONAL_PAYMENT_REF";
    private static final String OBTAIN_ADDIIONAL_FEE_REFERENCE_ID = "ObtainAdditionalPaymentReference";

    private static final String CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION = "CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION";
    private static final String CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID
        = "makeDecisionCreateDashboardNotificationForApplicant";

    private static final String CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION = "CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION";
    private static final String CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID
        = "makeDecisionCreateDashboardNotificationForRespondent";

    public JudgeMakesDecisionGeneralApplicationTest() {
        super("judge_makes_decision_general_application.bpmn", "GA_MAKE_DECISION_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteCreatePDFDocument_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );
        //Obtain Additional Fee Value
        ExternalTask additionalFeeValueProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            additionalFeeValueProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            OBTAIN_ADDITIONAL_FEE_VALUE_EVENT,
            OBTAIN_ADDITIONAL_FEE_VALUE_ID
        );

        //Obtain Additional Payment Reference
        ExternalTask additionalPaymentRefProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            additionalPaymentRefProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            OBTAIN_ADDIIONAL_FEE_REFERENCE_EVENT,
            OBTAIN_ADDIIONAL_FEE_REFERENCE_ID
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            MAKE_DECISION_CASE_EVENT,
            CREATE_PDF_EVENT,
            CREATE_PDF_ID
        );

        //Complete add pdf to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID
        );

        //Complete Applicant Notification event
        ExternalTask judicialApplicantNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialApplicantNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_APPLICANT_NOTIFICATION_PROCESS_ID
        );

        //Complete Respondent Notification event
        ExternalTask judicialRespondentNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialRespondentNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_RESPONDENT_NOTIFICATION_PROCESS_ID
        );

        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION,
            CREATE_APPLICANT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID
        );

        dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION,
            CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_FOR_MAKE_DECISION_ACTIVITY_ID
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
