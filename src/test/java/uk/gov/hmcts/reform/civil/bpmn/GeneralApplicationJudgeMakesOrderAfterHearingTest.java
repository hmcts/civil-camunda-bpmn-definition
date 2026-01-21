package uk.gov.hmcts.reform.civil.bpmn;

import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseJudgeGASpecTest.MAKE_DECISION_CASE_EVENT;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseJudgeGASpecTest.UPDATE_FROM_GA_CASE_EVENT;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.WELSH_ENABLED;

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

    private static final String DASHBOARD_NOTIFICATION_EVENT = "DASHBOARD_NOTIFICATION_EVENT";
    private static final String DASHBOARD_NOTIFICATION_ACTIVITY = "GenerateDashboardNotificationsGenerateDirectionsOrder";

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
        endBusinessProcessWithoutTaskDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "end_general_application_business_process_without_WA_task.bpmn"))
            .deploy();
        deployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
                .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldCompleteGenerateDirectionsOrder_whenWelshDisabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            WELSH_ENABLED, false));

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

        ExternalTask dashboardNotifications = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotifications,
            MAKE_DECISION_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            DASHBOARD_NOTIFICATION_ACTIVITY,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldCompleteGenerateDirectionsOrder_whenWelshEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            WELSH_ENABLED, true));

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

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS_WITHOUT_TASK);
        completeBusinessProcessForGADocUpload(endBusinessProcess);
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
