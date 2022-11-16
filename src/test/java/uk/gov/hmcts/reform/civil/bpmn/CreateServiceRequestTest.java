package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CreateServiceRequestTest extends BpmnBaseTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "CREATE_SERVICE_REQUEST";
    private static final String PROCESS_ID = "CREATE_CLAIM_PROCESS_ID_SPEC";
    private static final String FLOW_STATE = "flowState";
    private static final String FLOW_FLAGS = "flowFlags";
    //assign case access
    private static final String CASE_ASSIGNMENT_EVENT = "ASSIGN_CASE_TO_APPLICANT_SOLICITOR1_SPEC";
    private static final String CASE_ASSIGNMENT_ACTIVITY_ID = "CaseAssignmentToApplicantSolicitor1ForSpec";
    //payment
    public static final String CREATE_SERVICE_REQUEST_API = "CREATE_SERVICE_REQUEST_API";
    private static final String MAKE_PAYMENT_ACTIVITY_ID = "serviceRequestAPI";
    private static final String PROCESS_PAYMENT_TOPIC = "processCaseEvent";

    enum FlowState {
        CLAIM_ISSUED_PAYMENT_FAILED,
        CLAIM_ISSUED_PAYMENT_SUCCESSFUL,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public CreateServiceRequestTest() {
        super("create_service_request.bpmn", "CREATE_CLAIM_PROCESS_ID_SPEC");
    }

    @Nested
    class PostFlowStateRename {

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenClaimRemainOnlineForUnrepresentedDefendant() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, Map.of(
                "RPA_CONTINUOUS_FEED", true,
                "PIP_ENABLED", true));

            startBusinessProcess(variables);

            //complete the case assignment process
            completeCaseAssignment(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenClaimOfflineMultipartyForUnrepresentedDefendant() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, Map.of("RPA_CONTINUOUS_FEED", true));

            //complete the start business process
            startBusinessProcess(variables);

            //complete the case assignment process
            completeCaseAssignment(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenClaimOfflineForUnregisteredDefendant() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, Map.of("RPA_CONTINUOUS_FEED", true));

            //complete the start business process
            startBusinessProcess(variables);

            //complete the case assignment process
            completeCaseAssignment(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldFailedCreateClaim_whenPaymentFailed() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, Map.of("RPA_CONTINUOUS_FEED", true));

            //complete the start business process
            startBusinessProcess(variables);

            //complete the case assignment process
            completeCaseAssignment(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            completePayment(variables);

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenClaimOnline() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, Map.of("RPA_CONTINUOUS_FEED", true));

            //complete the start business process
            startBusinessProcess(variables);

            //complete the case assignment process
            completeCaseAssignment(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }
    }

    private void completeCaseAssignment(VariableMap variables) {
        ExternalTask caseAssignment = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            caseAssignment,
            PROCESS_CASE_EVENT,
            CASE_ASSIGNMENT_EVENT,
            CASE_ASSIGNMENT_ACTIVITY_ID,
            variables
        );
    }

    public void startBusinessProcess(VariableMap variables) {
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
    }

    public void completePayment(VariableMap variables) {
        ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
        assertCompleteExternalTask(
            paymentTask,
            PROCESS_PAYMENT_TOPIC,
            CREATE_SERVICE_REQUEST_API,
            MAKE_PAYMENT_ACTIVITY_ID,
            variables
        );
    }

}
