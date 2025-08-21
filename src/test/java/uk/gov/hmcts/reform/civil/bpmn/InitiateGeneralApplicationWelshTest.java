package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class InitiateGeneralApplicationWelshTest extends BpmnBaseGASpecTest {

    public static final String END_BUSINESS_PROCESS = "END_BUSINESS_PROCESS_GASPEC_WITHOUT_WA_TASK";
    //BPMN Settings
    private static final String MESSAGE_NAME = "INITIATE_GENERAL_APPLICATION";
    private static final String PROCESS_ID = "GA_INITIATE_PROCESS_ID";
    //create general application Case
    private static final String CREATE_GENERAL_APPLICATION_EVENT = "CREATE_GENERAL_APPLICATION_CASE";
    private static final String CREATE_GENERAL_APPLICATION_ID = "CreateGeneralApplicationCase";
    //Link General Application Case to Parent Case
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT
        = "LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE";
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID
        = "LinkGeneralApplicationCaseToParentCase";
    //Assigning of roles
    private static final String ASSIGNIN_OF_ROLES_EVENT = "ASSIGN_GA_ROLES";
    private static final String ASSIGNIN_OF_ROLES_ID = "AssigningOfRoles";
    //Fee Validation
    private static final String VALIDATE_FEE_EVENT = "VALIDATE_FEE_GASPEC";
    private static final String VALIDATE_FEE_ID = "GeneralApplicationValidateFee";
    //Make Service Request
    private static final String MAKE_SERVICE_REQ_EVENT = "MAKE_PAYMENT_SERVICE_REQ_GASPEC";
    private static final String MAKE_SERVICE_REQ_ID = "GeneralApplicationPaymentServiceReq";
    //Make Service Request
    private static final String GENERATE_DRAFT_DOC_EVENT = "GENERATE_DRAFT_DOCUMENT";
    private static final String GENERATE_DRAFT_DOC_ID = "DraftDocumentGenerator";
    private static final String FREE_FEE_WELSH_APPLICATION = "FREE_FEE_WELSH_APPLICATION";
    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";

    public InitiateGeneralApplicationWelshTest() {
        super("initiate_general_application.bpmn", "GA_INITIATE_PROCESS_ID");
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(
                DIAGRAM_PATH,
                "start_initiate_ga_business_process_in_civil.bpmn"
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
    void shouldSuccessfullyCompleteCreateGeneralApplicationForFreeFeeApp_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            FREE_FEE_WELSH_APPLICATION, true));

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
        ExternalTask documentGeneration = assertNextExternalTask(CREATE_APPLICATION_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            CREATE_APPLICATION_CASE_EVENT,
            CREATE_GENERAL_APPLICATION_EVENT,
            CREATE_GENERAL_APPLICATION_ID,
            variables
        );

        //link general application case to parent case
        ExternalTask linkCases = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            linkCases,
            APPLICATION_EVENT_GASPEC,
            LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT,
            LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID,
            variables
        );

        //assigne of roles
        ExternalTask assigneRoles = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            assigneRoles,
            APPLICATION_EVENT_GASPEC,
            ASSIGNIN_OF_ROLES_EVENT,
            ASSIGNIN_OF_ROLES_ID,
            variables
        );

        //validate fee
        ExternalTask validateFee = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            validateFee,
            APPLICATION_EVENT_GASPEC,
            VALIDATE_FEE_EVENT,
            VALIDATE_FEE_ID,
            variables
        );
        //make service request
        ExternalTask makeServiceRequest = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            makeServiceRequest,
            APPLICATION_EVENT_GASPEC,
            MAKE_SERVICE_REQ_EVENT,
            MAKE_SERVICE_REQ_ID,
            variables
        );

        //make draft doc create request
        ExternalTask generateDraftDoc = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            generateDraftDoc,
            APPLICATION_EVENT_GASPEC,
            GENERATE_DRAFT_DOC_EVENT,
            GENERATE_DRAFT_DOC_ID,
            variables
        );
       //add application to translation
        ExternalTask applicationToTranslationSection = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            applicationToTranslationSection,
            APPLICATION_EVENT_GASPEC,
            "ADD_APPLICATION_TO_TRANSLATION_COLLECTION",
            "applicationTranslationCollectionId",
            variables
        );

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

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcessForGADocUpload(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
