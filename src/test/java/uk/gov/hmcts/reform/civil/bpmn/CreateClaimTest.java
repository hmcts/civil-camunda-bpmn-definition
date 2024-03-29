package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateClaimTest extends BpmnBaseTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "CREATE_CLAIM";
    private static final String PROCESS_ID = "CREATE_CLAIM_PROCESS_ID";
    private static final String FLOW_STATE = "flowState";
    private static final String FLOW_FLAGS = "flowFlags";
    //assign case access
    private static final String CASE_ASSIGNMENT_EVENT = "ASSIGN_CASE_TO_APPLICANT_SOLICITOR1";
    private static final String CASE_ASSIGNMENT_ACTIVITY_ID = "CaseAssignmentToApplicantSolicitor1";
    //validate fee
    private static final String VALIDATE_FEE_EVENT = "VALIDATE_FEE";
    private static final String VALIDATE_FEE_ACTIVITY_ID = "ValidateClaimFee";
    //payment
    public static final String MAKE_PBA_PAYMENT_EVENT = "MAKE_PBA_PAYMENT";
    private static final String MAKE_PAYMENT_ACTIVITY_ID = "CreateClaimMakePayment";
    private static final String PROCESS_PAYMENT_TOPIC = "processPayment";
    //generate claim form
    private static final String GENERATE_CLAIM_FORM_EVENT = "GENERATE_CLAIM_FORM";
    private static final String GENERATE_CLAIM_FORM_ACTIVITY_ID = "GenerateClaimForm";
    //proceed offline
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    public static final String PROCEED_OFFLINE_FOR_UNREPRESENTED_SOLICITOR_ACTIVITY_ID
        = "ProceedOfflineUnrepresentedSolicitor";
    public static final String PROCEED_OFFLINE_FOR_UNREGISTERED_SOLICITOR_ACTIVITY_ID
        = "ProceedOfflineForUnregisteredFirm";
    public static final String PROCEED_OFFLINE_FOR_UNREPRESENTED_UNREGISTERED_SOLICITOR_ACTIVITY_ID
        = "ProceedOfflineForUnRepresentedSolicitorUnRegisteredFirm";
    public static final String
        CREATE_CLAIM_PROCEEDS_OFFLINE_NOTIFY_APPLICANT_SOLICITOR_1_FOR_UNREPRESENTED_SOLICITOR_UNREGISTERED_FIRM
        = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForUnRepresentedSolicitorUnRegisteredFirm";
    public static final String CREATE_CLAIM_PROCEEDS_OFFLINE_NOTIFY_APPLICANT_SOLICITOR_1_ACTIVITY_ID_FOR_UNREG_FIRM
        = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForUnRegisteredFirm";
    //notification - handed offline
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN_EVENT =
        "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN";
    private static final String NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE_UNREPRESENTED_ACTIVITY_ID
        = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForUnRepresentedSolicitor";
    public static final String NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE_NOT_REGISTERED_ACTIVITY_ID
        = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForUnRegisteredFirm";
    //claim issued
    public static final String ISSUE_CLAIM_EVENT = "PROCESS_CLAIM_ISSUE";
    private static final String ISSUE_CLAIM_ACTIVITY_ID = "IssueClaim";
    private static final String ISSUE_CLAIM_UNREPRESENTED_RESPONDENT_ACTIVITY_ID = "IssueClaimUnrepresentedRespondent";
    //failed payment
    public static final String PAYMENT_FAILED_EVENT = "PROCESS_PAYMENT_FAILED";
    private static final String PAYMENT_FAILED_ACTIVITY_ID = "PaymentFailed";
    //notification - failed payment
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT_EVENT
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT_ACTIVITY_ID
        = "CreateClaimPaymentFailedNotifyApplicantSolicitor1";
    //rpa
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    enum FlowState {
        CLAIM_ISSUED_PAYMENT_FAILED,
        PAYMENT_FAILED,
        CLAIM_ISSUED_PAYMENT_SUCCESSFUL,
        PAYMENT_SUCCESSFUL,
        PENDING_CLAIM_ISSUED,
        AWAITING_CASE_NOTIFICATION,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT,
        PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public CreateClaimTest() {
        super("create_claim.bpmn", "CREATE_CLAIM_PROCESS_ID");
    }

    @Nested
    class PostFlowStateRename {

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenPaymentWasSuccessful() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();

            //complete the start business process
            ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
            assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
            );

            //complete the case assignment process
            completeCaseAssignment(variables);

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
            assertCompleteExternalTask(
                paymentTask,
                PROCESS_PAYMENT_TOPIC,
                MAKE_PBA_PAYMENT_EVENT,
                MAKE_PAYMENT_ACTIVITY_ID,
                variables
            );

            //complete the document generation
            variables.putValue(FLOW_STATE, FlowState.PENDING_CLAIM_ISSUED.fullName());
            ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                documentGeneration,
                PROCESS_CASE_EVENT,
                GENERATE_CLAIM_FORM_EVENT,
                GENERATE_CLAIM_FORM_ACTIVITY_ID,
                variables
            );

            //complete the claim issue
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                ISSUE_CLAIM_EVENT,
                ISSUE_CLAIM_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE",
                "CreateClaimContinuingOnlineNotifyApplicantSolicitor1"
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                "NOTIFY_RPA_ON_CONTINUOUS_FEED",
                "NotifyRoboticsOnContinuousFeed",
                variables
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenPaymentFailed() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage("CREATE_CLAIM").getKey())
                .isEqualTo("CREATE_CLAIM_PROCESS_ID");

            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, FlowState.PENDING_CLAIM_ISSUED.fullName());

            //complete the start business process
            ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
            assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
            );

            //complete the case assignment process
            completeCaseAssignment(variables);

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_FAILED.fullName());
            ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
            assertCompleteExternalTask(
                paymentTask,
                PROCESS_PAYMENT_TOPIC,
                MAKE_PBA_PAYMENT_EVENT,
                MAKE_PAYMENT_ACTIVITY_ID,
                variables
            );

            //complete the payment failed
            ExternalTask paymentFailed = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                paymentFailed,
                PROCESS_CASE_EVENT,
                PAYMENT_FAILED_EVENT,
                PAYMENT_FAILED_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT_EVENT,
                NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenClaimTakenOfflineForUnregisteredDefendant() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();

            //complete the start business process
            ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
            assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
            );

            //complete the case assignment process
            completeCaseAssignment(variables);

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
            assertCompleteExternalTask(
                paymentTask,
                PROCESS_PAYMENT_TOPIC,
                MAKE_PBA_PAYMENT_EVENT,
                MAKE_PAYMENT_ACTIVITY_ID,
                variables
            );

            //complete the document generation
            variables.putValue(FLOW_STATE, FlowState.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT.fullName());
            ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                documentGeneration,
                PROCESS_CASE_EVENT,
                GENERATE_CLAIM_FORM_EVENT,
                GENERATE_CLAIM_FORM_ACTIVITY_ID,
                variables
            );

            //Take offline
            ExternalTask takeOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                takeOffline,
                PROCESS_CASE_EVENT,
                PROCEEDS_IN_HERITAGE_SYSTEM_EVENT,
                PROCEED_OFFLINE_FOR_UNREGISTERED_SOLICITOR_ACTIVITY_ID
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN_EVENT,
                NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE_NOT_REGISTERED_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenClaimTakenOfflineForUnrepresentedDefAndUnregisteredDef() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();

            //complete the start business process
            ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
            assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
            );

            //complete the case assignment process
            completeCaseAssignment(variables);

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
            assertCompleteExternalTask(
                paymentTask,
                PROCESS_PAYMENT_TOPIC,
                MAKE_PBA_PAYMENT_EVENT,
                MAKE_PAYMENT_ACTIVITY_ID,
                variables
            );

            //complete the document generation
            variables.putValue(FLOW_STATE,
                               FlowState.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName());
            ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                documentGeneration,
                PROCESS_CASE_EVENT,
                GENERATE_CLAIM_FORM_EVENT,
                GENERATE_CLAIM_FORM_ACTIVITY_ID,
                variables
            );

            //Take offline
            ExternalTask takeOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                takeOffline,
                PROCESS_CASE_EVENT,
                PROCEEDS_IN_HERITAGE_SYSTEM_EVENT,
                PROCEED_OFFLINE_FOR_UNREPRESENTED_UNREGISTERED_SOLICITOR_ACTIVITY_ID
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN_EVENT,
                CREATE_CLAIM_PROCEEDS_OFFLINE_NOTIFY_APPLICANT_SOLICITOR_1_FOR_UNREPRESENTED_SOLICITOR_UNREGISTERED_FIRM
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
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

        @Test
        void shouldSuccessfullyCompleteCreateClaim_whenInvoked() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();

            //complete the start business process
            ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
            assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
            );

            //complete the case assignment process
            completeCaseAssignment(variables);

            //validate the fee
            validateFee(variables);

            //complete the payment
            variables.putValue(FLOW_STATE, FlowState.CLAIM_ISSUED_PAYMENT_SUCCESSFUL.fullName());
            ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
            assertCompleteExternalTask(
                paymentTask,
                PROCESS_PAYMENT_TOPIC,
                MAKE_PBA_PAYMENT_EVENT,
                MAKE_PAYMENT_ACTIVITY_ID,
                variables
            );

            //complete the document generation
            variables.putValue(FLOW_STATE, FlowState.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT.fullName());
            ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                documentGeneration,
                PROCESS_CASE_EVENT,
                GENERATE_CLAIM_FORM_EVENT,
                GENERATE_CLAIM_FORM_ACTIVITY_ID,
                variables
            );

            //complete the claim issue
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                ISSUE_CLAIM_EVENT,
                ISSUE_CLAIM_UNREPRESENTED_RESPONDENT_ACTIVITY_ID,
                variables
            );

            //complete the notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_CONTINUING_ONLINE",
                "CreateClaimContinuingOnlineUnrepresentedRespondent"
            );

            //Notify RPA
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                "NOTIFY_RPA_ON_CONTINUOUS_FEED",
                "NotifyRoboticsOnContinuousFeed",
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
}
