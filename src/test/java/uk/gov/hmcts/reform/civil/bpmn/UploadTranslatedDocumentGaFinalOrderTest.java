package uk.gov.hmcts.reform.civil.bpmn;

import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseJudgeGASpecTest.MAKE_DECISION_CASE_EVENT;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseJudgeGASpecTest.UPDATE_FROM_GA_CASE_EVENT;

public class UploadTranslatedDocumentGaFinalOrderTest extends BpmnBaseGASpecTest {

    private static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_FINAL_ORDER";
    private static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOC_GA_FINAL_ORDER_PROCESS_ID";
    //start
    public static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    public static final String START_BUSINESS_EVENT = "START_GA_BUSINESS_PROCESS";
    public static final String START_BUSINESS_ACTIVITY = "StartGeneralApplicationBusinessProcessTaskId";
    //Add PDF document to main case
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "LinkDocumentToParentCase";

    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";

    private static final String DASHBOARD_NOTIFICATION_EVENT = "DASHBOARD_NOTIFICATION_EVENT";
    private static final String DASHBOARD_NOTIFICATION_ACTIVITY = "GenerateDashboardNotificationsGenerateDirectionsOrder";

    private static final String BULK_PRINT_APPLICANT_EVENT = "SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT";
    private static final String BULK_PRINT_APPLICANT_ACTIVITY = "BulkPrintOrderApplicant";

    private static final String BULK_PRINT_RESPONDENT_EVENT = "SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT";
    private static final String BULK_PRINT_RESPONDENT_ACTIVITY = "BulkPrintOrderRespondent";

    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";
    private static final String APPLICATION_EVENT_GASPEC = "applicationEventGASpec";

    public UploadTranslatedDocumentGaFinalOrderTest() {
        super("upload_translated_document_ga_final_order.bpmn", "UPLOAD_TRANSLATED_DOC_GA_FINAL_ORDER_PROCESS_ID");
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH,
                                                "start_business_process_in_general_application.bpmn"))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "end_general_application_business_process.bpmn"))
            .deploy();
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteNotifications_whenCalled(boolean isLipRespondent) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_RESPONDENT, isLipRespondent));

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

        ExternalTask dashboardNotifications = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotifications,
            MAKE_DECISION_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            DASHBOARD_NOTIFICATION_ACTIVITY,
            variables
        );

        ExternalTask bulkPrintApplicant = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
            bulkPrintApplicant,
            MAKE_DECISION_CASE_EVENT,
            BULK_PRINT_APPLICANT_EVENT,
            BULK_PRINT_APPLICANT_ACTIVITY,
            variables
        );

        if (isLipRespondent) {
            ExternalTask bulkPrintRespondent = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
            assertCompleteExternalTask(
                bulkPrintRespondent,
                MAKE_DECISION_CASE_EVENT,
                BULK_PRINT_RESPONDENT_EVENT,
                BULK_PRINT_RESPONDENT_ACTIVITY,
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        if (isLipRespondent) {
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
