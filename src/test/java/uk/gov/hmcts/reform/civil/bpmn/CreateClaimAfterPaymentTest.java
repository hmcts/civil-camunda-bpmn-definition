package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateClaimAfterPaymentTest extends BpmnBaseTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "CREATE_CLAIM_AFTER_PAYMENT";
    private static final String PROCESS_ID = "CREATE_CLAIM_AFTER_PAYMENT_PROCESS_ID";
    private static final String FLOW_STATE = "flowState";
    //generate claim form
    private static final String GENERATE_CLAIM_FORM_EVENT = "GENERATE_CLAIM_FORM";
    private static final String GENERATE_CLAIM_FORM_ACTIVITY_ID = "GenerateClaimForm";
    //proceed offline
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    public static final String PROCEED_OFFLINE_FOR_UNREGISTERED_SOLICITOR_ACTIVITY_ID
            = "ProceedOfflineForUnregisteredFirm";
    public static final String PROCEED_OFFLINE_FOR_UNREPRESENTED_UNREGISTERED_SOLICITOR_ACTIVITY_ID
            = "ProceedOfflineForUnRepresentedSolicitorUnRegisteredFirm";
    //claim issued
    public static final String ISSUE_CLAIM_EVENT = "PROCESS_CLAIM_ISSUE";
    private static final String ISSUE_CLAIM_ACTIVITY_ID = "IssueClaim";
    private static final String ISSUE_CLAIM_UNREPRESENTED_RESPONDENT_ACTIVITY_ID = "IssueClaimUnrepresentedRespondent";
    //rpa
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String CREATE_CLAIM_AFTER_PAYMENT_CONTINUING_ONLINE_NOTIFIER = "Notifyapplicantsolicitor1";
    public static final String CREATE_CLAIM_AFTER_PAYMENT_CONTINUING_OFFLINE_NOTIFIER = "Notifyapplicantsolicitor1";
    public static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    public static final String NOTIFY_ROBOTICS_ON_CONTINUOUS_FEED = "NotifyRoboticsOnContinuousFeed";

    enum FlowState {
        PENDING_CLAIM_ISSUED,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public CreateClaimAfterPaymentTest() {
        super("create_claim_after_payment.bpmn", "CREATE_CLAIM_AFTER_PAYMENT_PROCESS_ID");
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
                    NOTIFY_EVENT,
                    CREATE_CLAIM_AFTER_PAYMENT_CONTINUING_ONLINE_NOTIFIER
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    forRobotics,
                    PROCESS_CASE_EVENT,
                    NOTIFY_RPA_ON_CONTINUOUS_FEED,
                    NOTIFY_ROBOTICS_ON_CONTINUOUS_FEED,
                    variables
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
                    NOTIFY_EVENT,
                    CREATE_CLAIM_AFTER_PAYMENT_CONTINUING_OFFLINE_NOTIFIER
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
                    NOTIFY_EVENT,
                    CREATE_CLAIM_AFTER_PAYMENT_CONTINUING_OFFLINE_NOTIFIER
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
        void shouldSuccessfullyCompleteCreateClaim() {
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
                    NOTIFY_EVENT,
                    CREATE_CLAIM_AFTER_PAYMENT_CONTINUING_ONLINE_NOTIFIER
            );

            //Notify RPA
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    forRobotics,
                    PROCESS_CASE_EVENT,
                    NOTIFY_RPA_ON_CONTINUOUS_FEED,
                    NOTIFY_ROBOTICS_ON_CONTINUOUS_FEED,
                    variables
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }
    }
}
