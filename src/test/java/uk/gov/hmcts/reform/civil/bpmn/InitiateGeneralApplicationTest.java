package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InitiateGeneralApplicationTest extends BpmnBaseGASpecTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "INITIATE_GENERAL_APPLICATION";
    private static final String PROCESS_ID = "INITIATE_GENERAL_APPLICATION_PROCESS_ID";
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

    public InitiateGeneralApplicationTest() {
        super("initiate_general_application.bpmn", "INITIATE_GENERAL_APPLICATION_PROCESS_ID");
    }

    enum FlowState {
        DRAFT,
        PAYMENT_FAILED,
        PAYMENT_SUCCESSFUL;
        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteCreateGeneralApplication_whenCalled(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of("RPA_CONTINUOUS_FEED", rpaContinuousFeed));

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
