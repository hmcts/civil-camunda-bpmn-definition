package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CreateClaimSpecTest extends BpmnBaseTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "CREATE_CLAIM_SPEC";
    private static final String PROCESS_ID = "CREATE_CLAIM_PROCESS_ID_SPEC";
    private static final String FLOW_STATE = "flowState";
    private static final String FLOW_FLAGS = "flowFlags";
    //assign case access
    private static final String CASE_ASSIGNMENT_EVENT = "ASSIGN_CASE_TO_APPLICANT_SOLICITOR1";
    private static final String CASE_ASSIGNMENT_ACTIVITY_ID = "CaseAssignmentToApplicantSolicitor1";
    //validate fee
    private static final String VALIDATE_FEE_EVENT = "VALIDATE_FEE_SPEC";
    private static final String VALIDATE_FEE_ACTIVITY_ID = "ValidateClaimFeeForSpec";
    //payment
    public static final String MAKE_PBA_PAYMENT_EVENT = "MAKE_PBA_PAYMENT_SPEC";
    private static final String MAKE_PAYMENT_ACTIVITY_ID = "CreateClaimMakePaymentForSpec";
    private static final String PROCESS_PAYMENT_TOPIC = "processPayment";
    //generate claim form
    private static final String GENERATE_CLAIM_FORM_EVENT = "GENERATE_CLAIM_FORM_SPEC";
    private static final String GENERATE_CLAIM_FORM_ACTIVITY_ID = "GenerateClaimFormForSpec";
    //issue claim
    private static final String PROCESS_CLAIM_ISSUE_EVENT = "PROCESS_CLAIM_ISSUE_SPEC";
    private static final String PROCESS_CLAIM_ISSUE_ACTIVITY_ID = "IssueClaimForSpec";
    private static final String PROCESS_CLAIM_ISSUE_UNREPRESENTED_ACTIVITY_ID
        = "IssueClaimForSpecUnrepresentedSolicitor";
    //notify applicant solicitor 1 continuing online
    private static final String NOTIFY_APPLICANT_SOLICITOR1_ONLINE_ISSUE_EVENT
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_ONLINE_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyApplicantSolicitor1ForSpec";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_ONLINE_UNREPRESENTED_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyApplicantSolicitor1ForSpecUnrepresented";
    //proceed offline
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_ISSUE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_UNREPRESENTED_ACTIVITY_ID
        = "ProceedOfflineForUnRepresentedSolicitor";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_UNREGISTERED_ACTIVITY_ID
        = "ProceedOfflineForUnregisteredFirm";
    //notify RPA
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    //notify RPA offline
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_EVENT = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    //notify applicant solicitor 1 unrepresented offline
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_EVENT
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_ACTIVITY_ID
        = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForUnRepresentedSolicitorForSpec";
    //notify applicant solicitor 1 unregistered offline
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC_EVENT
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC_ACTIVITY_ID
        = "TakeCaseOfflineForSpecNotifyApplicantSolicitor1";
    //notify applicant solicitor 1 payment fail
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC_EVENT
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC_ACTIVITY_ID
        = "CreateClaimPaymentFailedForSpecNotifyApplicantSolicitor1";
    //notify respondent solicitor 1
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyRespondentSolicitor1ForSpec";
    //notify respondent solicitor 2
    private static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyRespondentSolicitor2ForSpec";
    //notify respondent 1
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyRespondent1ForSpec";
    //payment failed
    private static final String PROCESS_PAYMENT_FAILED_EVENT = "PROCESS_PAYMENT_FAILED";
    private static final String PROCESS_PAYMENT_FAILED_ACTIVITY_ID = "PaymentFailed";

    enum FlowState {
        CLAIM_ISSUED_PAYMENT_FAILED,
        PAYMENT_FAILED,
        CLAIM_ISSUED_PAYMENT_SUCCESSFUL,
        PAYMENT_SUCCESSFUL,
        PENDING_CLAIM_ISSUED,
        AWAITING_CASE_NOTIFICATION,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public CreateClaimSpecTest() {
        super("create_claim_spec.bpmn", "CREATE_CLAIM_PROCESS_ID_SPEC");
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

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //complete the document generation
            variables.putValue(
                FLOW_STATE,
                FlowState.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC.fullName()
            );
            documentGeneration(variables);

            //complete the claim issue
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                PROCESS_CLAIM_ISSUE_EVENT,
                PROCESS_CLAIM_ISSUE_UNREPRESENTED_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_ONLINE_ISSUE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_ONLINE_UNREPRESENTED_ACTIVITY_ID
            );

            //complete the respondent notification
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationRespondentTask,
                PROCESS_CASE_EVENT,
                NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
                NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
                variables
            );

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

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //complete the document generation
            variables.putValue(
                FLOW_STATE,
                FlowState.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName()
            );
            documentGeneration(variables);

            //proceed offline
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                PROCEEDS_IN_HERITAGE_SYSTEM_ISSUE_EVENT,
                PROCEEDS_IN_HERITAGE_SYSTEM_UNREPRESENTED_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID,
                variables
            );

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

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //complete the document generation
            variables.putValue(
                FLOW_STATE,
                FlowState.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName()
            );
            documentGeneration(variables);

            //proceed offline
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                PROCEEDS_IN_HERITAGE_SYSTEM_ISSUE_EVENT,
                PROCEEDS_IN_HERITAGE_SYSTEM_UNREGISTERED_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE_SPEC_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID,
                variables
            );

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

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            completePayment(variables);

            //payment failed
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                PROCESS_PAYMENT_FAILED_EVENT,
                PROCESS_PAYMENT_FAILED_ACTIVITY_ID,
                variables
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT_SPEC_ACTIVITY_ID,
                variables
            );

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

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            completePayment(variables);

            //complete the document generation
            variables.putValue(
                FLOW_STATE,
                FlowState.PENDING_CLAIM_ISSUED.fullName()
            );
            documentGeneration(variables);

            //complete the claim issue
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                PROCESS_CLAIM_ISSUE_EVENT,
                PROCESS_CLAIM_ISSUE_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_ONLINE_ISSUE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_ONLINE_ACTIVITY_ID
            );

            //complete the respondent 1 notification
            ExternalTask notificationRespondent1Task = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationRespondent1Task,
                PROCESS_CASE_EVENT,
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID,
                variables
            );

            //complete the respondent 2 notification
            ExternalTask notificationRespondent2Task = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationRespondent2Task,
                PROCESS_CASE_EVENT,
                NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
                NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID,
                variables
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
                variables
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }
    }

    private void validateFee(VariableMap variables) {
        ExternalTask feeTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            feeTask,
            PROCESS_CASE_EVENT,
            VALIDATE_FEE_EVENT,
            VALIDATE_FEE_ACTIVITY_ID,
            variables
        );
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

    public void completePayment(VariableMap variables) {
        ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
        assertCompleteExternalTask(
            paymentTask,
            PROCESS_PAYMENT_TOPIC,
            MAKE_PBA_PAYMENT_EVENT,
            MAKE_PAYMENT_ACTIVITY_ID,
            variables
        );
    }

    public void documentGeneration(VariableMap variables) {
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_CLAIM_FORM_EVENT,
            GENERATE_CLAIM_FORM_ACTIVITY_ID,
            variables
        );
    }
}
