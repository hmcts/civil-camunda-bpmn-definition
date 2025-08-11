package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class InitiateGeneralApplicationAfterPaymentForWelshTest extends BpmnBaseGASpecTest {

    public static final String END_BUSINESS_PROCESS = "END_BUSINESS_PROCESS_GASPEC_WITHOUT_WA_TASK";
    public static final String APPLICATION_PROCESS_CASE_EVENT = "applicationProcessCaseEventGASpec";
    private static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    private static final String WELSH_ENABLED = "WELSH_ENABLED";
    private static final String ASSIGNIN_OF_ROLES_EVENT = "ASSIGN_GA_ROLES";
    private static final String ASSIGNIN_OF_ROLES_ID = "AssigningOfRoles";
    private static final String GENERATE_DRAFT_DOCUMENT = "GENERATE_DRAFT_DOCUMENT";
    private static final String GENERATE_DRAFT_DOCUMENT_ID = "GenerateDraftDocumentId";
    public static final String UPDATE_FROM_GA_CASE_EVENT = "updateFromGACaseEvent";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDraftDocToMainCaseID";

    public InitiateGeneralApplicationAfterPaymentForWelshTest() {
        super("initiate_general_application_after_payment.bpmn", "GA_INITIATE_AFTER_PAYMENT_PROCESS_ID");
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(
                DIAGRAM_PATH,
                "start_business_process_in_general_application.bpmn"
            ))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(
                DIAGRAM_PATH,
                "end_general_application_business_process_without_WA_task.bpmn"
            ))
            .deploy();
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldPauseNotificationAndStateChangeForWelshApplication() {
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            WELSH_ENABLED, "true"));

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

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcessForGADocUpload(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
