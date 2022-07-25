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

class NotifyClaimTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_DEFENDANT_OF_CLAIM";
    public static final String PROCESS_ID = "NOTIFY_CLAIM";

    //CCD EVENTS
    public static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_FOR_CLAIM_ISSUE_CC
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2_FOR_CLAIM_ISSUE
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE";
    private static final String NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG
        = "NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG";
    private static final String NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG
        = "NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_ACTIVITY_ID
        = "NotifyDefendantSolicitor1";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_CC_ACTIVITY_ID
        = "NotifyApplicantSolicitorCC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2_CLAIM_ISSUE_ACTIVITY_ID
        = "NotifyDefendantSolicitor2";
    private static final String NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG_ACTIVITY_ID
        = "NotifyClaimCAARespondent1Org";
    private static final String NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG_ACTIVITY_ID
        = "NotifyClaimCAARespondent2Org";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID = "ProceedOffline";
    private static final String NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE_ACTIVITY_ID
        = "NotifyClaimProceedsOfflineNotifyApplicantSolicitor1";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    enum FlowState {
        CLAIM_NOTIFIED,
        TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public NotifyClaimTest() {
        super("notify_claim.bpmn", "NOTIFY_CLAIM");
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteNotifyClaim_andRpaContinuousFeed_whenCalled(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, FlowState.CLAIM_NOTIFIED.fullName());
        variables.putValue(FLOW_FLAGS, Map.of(RPA_CONTINUOUS_FEED, rpaContinuousFeed));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_ACTIVITY_ID,
                                   variables
        );

        //complete the respondent1 CAA notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG,
                                   NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG_ACTIVITY_ID,
                                   variables
        );

        //complete the CC notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_FOR_CLAIM_ISSUE_CC,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_CC_ACTIVITY_ID,
                                   variables
        );

        if (rpaContinuousFeed) {
            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteNotifyClaim_AndNotifyAppropriateSolicitors_whenCalled(
        Boolean twoRespondentRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, FlowState.CLAIM_NOTIFIED.fullName());
        variables.putValue(FLOW_FLAGS, Map.of(TWO_RESPONDENT_REPRESENTATIVES, twoRespondentRepresentatives,
                                              RPA_CONTINUOUS_FEED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_ACTIVITY_ID,
                                   variables
        );

        //complete the respondent1 CAA notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG,
                                   NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG_ACTIVITY_ID,
                                   variables
        );

        //complete the CC notification
        ExternalTask notificationCcTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationCcTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_FOR_CLAIM_ISSUE_CC,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_CC_ACTIVITY_ID,
                                   variables
        );

        if (twoRespondentRepresentatives) {
            //complete the additional defendant solicitor notification
            ExternalTask secondSolicitorNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(secondSolicitorNotificationTask,
                                       PROCESS_CASE_EVENT,
                                       NOTIFY_RESPONDENT_SOLICITOR_2_FOR_CLAIM_ISSUE,
                                       NOTIFY_RESPONDENT_SOLICITOR_2_CLAIM_ISSUE_ACTIVITY_ID,
                                       variables
            );

            //complete the respondent2 CAA notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask,
                                       PROCESS_CASE_EVENT,
                                       NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG,
                                       NOTIFY_CLAIM_CAA_RESPONDENT_2_ORG_ACTIVITY_ID,
                                       variables
            );
        }

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteNotifyClaim_TakenOffline() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, FlowState.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_ACTIVITY_ID,
                                   variables
        );

        //complete the respondent1 CAA notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG,
                                   NOTIFY_CLAIM_CAA_RESPONDENT_1_ORG_ACTIVITY_ID,
                                   variables
        );

        //complete the CC notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_FOR_CLAIM_ISSUE_CC,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_CC_ACTIVITY_ID,
                                   variables
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(proceedOfflineTask,
                                   PROCESS_CASE_EVENT,
                                   PROCEEDS_IN_HERITAGE_SYSTEM,
                                   PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //Notify Claimant Solicitor - Handed Offline
        ExternalTask notifyApplicantSolicitorHandedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyApplicantSolicitorHandedOffline,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE,
                                   NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE_ACTIVITY_ID
        );

        //Notify RPA - Handed Offline
        ExternalTask notifyRpaHandedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyRpaHandedOffline,
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
}
