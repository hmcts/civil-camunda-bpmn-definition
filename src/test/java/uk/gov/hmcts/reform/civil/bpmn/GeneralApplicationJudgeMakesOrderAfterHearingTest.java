package uk.gov.hmcts.reform.civil.bpmn;

import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseJudgeGASpecTest.MAKE_DECISION_CASE_EVENT;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseJudgeGASpecTest.UPDATE_FROM_GA_CASE_EVENT;

public class GeneralApplicationJudgeMakesOrderAfterHearingTest extends BpmnBaseGASpecTest {

    private static final String MESSAGE_NAME = "GENERATE_DIRECTIONS_ORDER";
    private static final String PROCESS_ID = "GA_GENERATE_DIRECTIONS_ORDER_ID";
    //start
    public static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    public static final String START_BUSINESS_EVENT = "START_GA_BUSINESS_PROCESS";
    public static final String START_BUSINESS_ACTIVITY = "StartGeneralApplicationBusinessProcessTaskId";
    //Create PDF
    private static final String CREATE_PDF_EVENT = "GENERATE_JUDGES_FORM";
    private static final String CREATE_PDF_ID = "CreatePDFDocument";
    //Add PDF document to main case
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "LinkDocumentToParentCase";

    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";

    private static final String CREATE_APPLICANT_DASHBOARD_NOTIFICATION_ORDER_MADE_EVENT = "CREATE_APPLICANT_DASHBOARD_NOTIFICATION_ORDER_MADE";
    private static final String CREATE_APPLICANT_DASHBOARD_NOTIFICATION_ORDER_MADE_ACTIVITY = "applicantNotificationForOrderMadeByJudge";

    private static final String CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE_EVENT = "CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE";
    private static final String CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE_ACTIVITY = "respondentNotificationForOrderMadeByJudge";

    private static final String UPDATE_CLAIMANT_GA_TASK_LIST_GA_COMPLETE_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA_COMPLETE";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ACTIVITY_ID = "GeneralApplicationClaimantTaskList";
    private static final String UPDATE_RESPONDENT_GA_TASK_LIST_GA_COMPLETE_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA_COMPLETE";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ACTIVITY_ID = "GeneralApplicationRespondentTaskList";

    public GeneralApplicationJudgeMakesOrderAfterHearingTest() {
        super("general_application_judge_makes_order_after_hearing.bpmn", "GA_GENERATE_DIRECTIONS_ORDER_ID");
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
    @CsvSource({"false,false", "true,false", "true,true", "false,true"})
    void shouldSuccessfullyCompleteCreatePDFDocument_whenCalled(boolean isLipApplicant, boolean isLipRespondent) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT , isLipApplicant,
            LIP_RESPONDENT , isLipRespondent));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
                documentGeneration,
                MAKE_DECISION_CASE_EVENT,
                CREATE_PDF_EVENT,
                CREATE_PDF_ID,
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

        if (isLipApplicant) {
            ExternalTask updateCuiClaimantDashboard = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
            assertCompleteExternalTask(
                updateCuiClaimantDashboard,
                MAKE_DECISION_CASE_EVENT,
                CREATE_APPLICANT_DASHBOARD_NOTIFICATION_ORDER_MADE_EVENT,
                CREATE_APPLICANT_DASHBOARD_NOTIFICATION_ORDER_MADE_ACTIVITY,
                variables
            );
        }

        if (isLipRespondent) {
            ExternalTask updateCuiDefendantDashboard = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
            assertCompleteExternalTask(
                updateCuiDefendantDashboard,
                MAKE_DECISION_CASE_EVENT,
                CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE_EVENT,
                CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_ORDER_MADE_ACTIVITY,
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        if (isLipApplicant || isLipRespondent) {
            ExternalTask claimantTaskListUpdate = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
            assertCompleteExternalTask(
                claimantTaskListUpdate,
                APPLICATION_EVENT_GASPEC,
                UPDATE_CLAIMANT_GA_TASK_LIST_GA_COMPLETE_EVENT,
                GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ACTIVITY_ID,
                variables
            );

            ExternalTask respondentTaskListUpdate = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
            assertCompleteExternalTask(
                respondentTaskListUpdate,
                APPLICATION_EVENT_GASPEC,
                UPDATE_RESPONDENT_GA_TASK_LIST_GA_COMPLETE_EVENT,
                GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ACTIVITY_ID,
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
