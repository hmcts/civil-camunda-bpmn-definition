package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CreateClaimSpecAfterPaymentTest extends BpmnBaseTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "CREATE_CLAIM_SPEC_AFTER_PAYMENT";
    private static final String PROCESS_ID = "CREATE_CLAIM_PROCESS_ID_SPEC_AFTER_PAYMENT";
    private static final String FLOW_STATE = "flowState";
    private static final String FLOW_FLAGS = "flowFlags";
    private static final String GENERATE_CLAIM_FORM_EVENT = "GENERATE_CLAIM_FORM_SPEC";
    private static final String GENERATE_CLAIM_FORM_ACTIVITY_ID = "GenerateClaimFormForSpec";
    //issue claim
    private static final String PROCESS_CLAIM_ISSUE_EVENT = "PROCESS_CLAIM_ISSUE_SPEC";
    private static final String PROCESS_CLAIM_ISSUE_ACTIVITY_ID = "IssueClaimForSpec";
    private static final String PROCESS_CLAIM_ISSUE_UNREPRESENTED_ACTIVITY_ID
            = "IssueClaimForSpecUnrepresentedSolicitor";

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

    public static final String SET_LIP_RESPONDENT_RESPONSE_DEADLINE_EVENT = "SET_LIP_RESPONDENT_RESPONSE_DEADLINE";
    private static final String SET_LIP_RESPONDENT_RESPONSE_DEADLINE_ACTIVITY_ID = "SetRespondent1Deadline";
    private static final String CREATE_ISSUE_CLAIM_DASHBOARD_NOTIFICATIONS_FOR_CLAIMANT1_ACTIVITY_ID = "CreateIssueClaimDashboardNotificationsForApplicant1";
    private static final String CREATE_ISSUE_CLAIM_DASHBOARD_NOTIFICATIONS_FOR_CLAIMANT1_EVENT = "CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1";
    private static final String GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC_EVENT = "GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC";
    private static final String GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC_ACTIVITY_ID = "GenerateLipClaimantClaimFormForSpec";
    private static final String GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC_EVENT = "GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC";
    private static final String GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC_ACTIVITY_ID = "GenerateLipDefendantClaimFormForSpec";

    private static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT_EVENT
            = "CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1";
    private static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT_ACTIVITY_ID
            = "CreateIssueClaimDashboardNotificationsForDefendant1";

    private static final String REMOVE_PAYMENT_DASHBOARD_NOTIFICATION_EVENT
            = "REMOVE_PAYMENT_DASHBOARD_NOTIFICATION";

    private static final String REMOVE_PAYMENT_DASHBOARD_NOTIFICATION_ACTIVITY_ID
            = "RemovePaymentDashboardNotification";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String CLAIM_SUBMISSION_NOTIFY_PARTIES = "ClaimSubmissionNotifyParties";
    public static final String CONTINUING_CLAIM_ONLINE_SPEC_CLAIM_NOTIFIER = "ContinuingClaimOnlineSpecClaimNotifier";
    public static final String GENERATE_PIP_LETTER = "GENERATE_PIP_LETTER";
    public static final String GENERATE_PIP_LETTER_ID = "GeneratePipLetter";
    public static final String TAKEN_OFFLINE_CASE_FOR_SPEC_NOTIFIER = "TakenOfflineCaseForSpecNotifier";
    public static final String RAISING_CLAIM_AGAINST_SPEC_LITIGANT_IN_PERSON_FOR_NOTIFIER = "RaisingClaimAgainstSpecLitigantInPersonForNotifier";

    public CreateClaimSpecAfterPaymentTest() {
        super("create_claim_spec_after_payment.bpmn", "CREATE_CLAIM_PROCESS_ID_SPEC_AFTER_PAYMENT");
    }

    enum FlowState {
        PENDING_CLAIM_ISSUED,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;

        public String fullName() {
            return "MAIN" + "." + name();
        }
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
                    "PIP_ENABLED", true,
                    "DASHBOARD_SERVICE_ENABLED", true));

            startBusinessProcess(variables);

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

            // complete the defendant dashboard notification
            ExternalTask dashboardNotificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    dashboardNotificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT_EVENT,
                    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT_ACTIVITY_ID
            );

            //complete the notifications
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    notificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    CONTINUING_CLAIM_ONLINE_SPEC_CLAIM_NOTIFIER
            );

            //complete generate PIP letter
            ExternalTask generatePipletter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    generatePipletter,
                    PROCESS_CASE_EVENT,
                    GENERATE_PIP_LETTER,
                    GENERATE_PIP_LETTER_ID
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
            variables.put(FLOW_FLAGS, null);

            //complete the start business process
            startBusinessProcess(variables);

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

            //complete the notifications
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    notificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    RAISING_CLAIM_AGAINST_SPEC_LITIGANT_IN_PERSON_FOR_NOTIFIER
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
            variables.put(FLOW_FLAGS, null);

            //complete the start business process
            startBusinessProcess(variables);

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

            //complete the notifications
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    notificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    TAKEN_OFFLINE_CASE_FOR_SPEC_NOTIFIER
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
        void shouldSuccessfullyCompleteCreateClaim_whenClaimOfflineForUnregisteredDef1UnrepresentedDef2Defendant() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, null);

            //complete the start business process
            startBusinessProcess(variables);

            //complete the document generation
            variables.putValue(
                    FLOW_STATE,
                    FlowState.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()
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

            //complete the notifications
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    notificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    TAKEN_OFFLINE_CASE_FOR_SPEC_NOTIFIER
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
        void shouldSuccessfullyCompleteCreateClaim_whenClaimOnline() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.put(FLOW_FLAGS, null);

            //complete the start business process
            startBusinessProcess(variables);

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

            //complete the notifications
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    notificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    CONTINUING_CLAIM_ONLINE_SPEC_CLAIM_NOTIFIER
            );

            //complete generate PIP letter
            ExternalTask generatePipletter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    generatePipletter,
                    PROCESS_CASE_EVENT,
                    GENERATE_PIP_LETTER,
                    GENERATE_PIP_LETTER_ID
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
        void shouldSuccessfullyCompleteCreateClaim_whenClaimIssued_UnregisteredDefendant() {

            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
            variables.put(FLOW_FLAGS, Map.of(
                    BULK_CLAIM_ENABLED, true,
                    LIP_CASE, true,
                    GENERAL_APPLICATION_ENABLED, true,
                    UNREPRESENTED_DEFENDANT_ONE, true,
                    PIP_ENABLED, true,
                    DASHBOARD_SERVICE_ENABLED, true
            ));

            //complete the start business process
            startBusinessProcess(variables);

            //Generate Lip claimant claim form
            ExternalTask generateLipClaimantClaimForm = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    generateLipClaimantClaimForm,
                    PROCESS_CASE_EVENT,
                    GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC_EVENT,
                    GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC_ACTIVITY_ID
            );

            //Generate Lip defendant claim form
            ExternalTask generateLipDefendantClaimForm = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    generateLipDefendantClaimForm,
                    PROCESS_CASE_EVENT,
                    GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC_EVENT,
                    GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC_ACTIVITY_ID
            );

            //Update Respondent response deadline date
            ExternalTask updateRespondentResponseDeadLine = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    updateRespondentResponseDeadLine,
                    PROCESS_CASE_EVENT,
                    SET_LIP_RESPONDENT_RESPONSE_DEADLINE_EVENT,
                    SET_LIP_RESPONDENT_RESPONSE_DEADLINE_ACTIVITY_ID
            );

            //complete the claim issue
            ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    claimIssue,
                    PROCESS_CASE_EVENT,
                    PROCESS_CLAIM_ISSUE_EVENT,
                    PROCESS_CLAIM_ISSUE_UNREPRESENTED_ACTIVITY_ID,
                    variables
            );

            // complete the defendant dashboard notification
            ExternalTask dashboardNotificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    dashboardNotificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT_EVENT,
                    CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT_ACTIVITY_ID
            );

            //complete the respondent notification
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    notificationRespondentTask,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    CONTINUING_CLAIM_ONLINE_SPEC_CLAIM_NOTIFIER
            );

            //complete generate PIP letter
            ExternalTask generatePipletter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    generatePipletter,
                    PROCESS_CASE_EVENT,
                    GENERATE_PIP_LETTER,
                    GENERATE_PIP_LETTER_ID
            );

            //complete the claimant dashboard notification
            ExternalTask claimantDashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    claimantDashboardNotificationTask,
                    PROCESS_CASE_EVENT,
                    CREATE_ISSUE_CLAIM_DASHBOARD_NOTIFICATIONS_FOR_CLAIMANT1_EVENT,
                    CREATE_ISSUE_CLAIM_DASHBOARD_NOTIFICATIONS_FOR_CLAIMANT1_ACTIVITY_ID
            );

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

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteCreateClaim_whenClaimIssuedIsBilingual(boolean welshEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
        variables.put(FLOW_FLAGS, Map.of(
                BULK_CLAIM_ENABLED, true,
                LIP_CASE, true,
                GENERAL_APPLICATION_ENABLED, true,
                UNREPRESENTED_DEFENDANT_ONE, true,
                PIP_ENABLED, true,
                CLAIM_ISSUE_BILINGUAL, true,
                WELSH_ENABLED, welshEnabled
        ));

        //complete the start business process
        startBusinessProcess(variables);

        //Generate Lip claimant claim form
        ExternalTask generateLipClaimantClaimForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                generateLipClaimantClaimForm,
                PROCESS_CASE_EVENT,
                GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC_EVENT,
                GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC_ACTIVITY_ID
        );

        //Generate Lip defendant claim form
        ExternalTask generateLipDefendantClaimForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                generateLipDefendantClaimForm,
                PROCESS_CASE_EVENT,
                GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC_EVENT,
                GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC_ACTIVITY_ID
        );

        //Notify Lip Claimant claim submission
        ExternalTask notifyLipClaimantClaimSubmission = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                notifyLipClaimantClaimSubmission,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                CLAIM_SUBMISSION_NOTIFY_PARTIES
        );

        if (welshEnabled) {
            //Delete payment notification
            ExternalTask removePaymentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    removePaymentNotification,
                    PROCESS_CASE_EVENT,
                    REMOVE_PAYMENT_DASHBOARD_NOTIFICATION_EVENT,
                    REMOVE_PAYMENT_DASHBOARD_NOTIFICATION_ACTIVITY_ID
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
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

