package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";
    public static final String SET_LIP_RESPONDENT_RESPONSE_DEADLINE_EVENT = "SET_LIP_RESPONDENT_RESPONSE_DEADLINE";
    private static final String SET_LIP_RESPONDENT_RESPONSE_DEADLINE_ACTIVITY_ID = "SetRespondent1Deadline";
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_NO_ACTIVITY_ID = "Activity_0ooszcc";

    public CreateClaimSpecAfterPaymentTest() {
        super("create_claim_spec_after_payment.bpmn", "CREATE_CLAIM_PROCESS_ID_SPEC_AFTER_PAYMENT");
    }

    enum FlowState {
        PENDING_CLAIM_ISSUED,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT,
        PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC,
        PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;

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
                "PIP_ENABLED", true));

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
                PIP_ENABLED, true
            ));

            //complete the start business process
            startBusinessProcess(variables);

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

            //complete the applicant notification
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
                NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
            );

            //complete the respondent notification
            ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationRespondentTask,
                PROCESS_CASE_EVENT,
                NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
                NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_NO_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }
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

