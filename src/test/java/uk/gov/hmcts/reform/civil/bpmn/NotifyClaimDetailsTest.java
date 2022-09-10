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

class NotifyClaimDetailsTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_DEFENDANT_OF_CLAIM_DETAILS";
    public static final String PROCESS_ID = "NOTIFY_CLAIM_DETAILS";

    //CCD CASE EVENT
    public static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS";
    public static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DETAILS_CC";
    public static final String NOTIFY_RESPONDENT_SOLICITOR_2_CLAIM_DETAILS
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DETAILS";

    private static final String NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN";

    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE =
        "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_ACTIVITY_ID
        = "NotifyClaimDetailsRespondentSolicitor1";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC_ACTIVITY_ID
        = "NotifyClaimDetailsApplicantSolicitor1CC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2_CLAIM_DETAILS_ACTIVITY_ID
        = "NotifyClaimDetailsRespondentSolicitor2";

    private static final String NOTIFY_CLAIM_DETAILS_OFFLINE_APPLICANT_SOLICITOR1_ACTIVITY_ID =
        "NotifyClaimDetailsProceedOfflineApplicantSolicitor1CC";

    private static final String NOTIFY_RPA_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";

    public NotifyClaimDetailsTest() {
        super("notify_claim_details.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteNotifyClaim_For1v1_OnRpa_whenCalled(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, "MAIN.CLAIM_DETAILS_NOTIFIED");
        variables.putValue(FLOW_FLAGS, Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, true,
                RPA_CONTINUOUS_FEED, rpaContinuousFeed,
                GENERAL_APPLICATION_ENABLED, false
        ));

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
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_ACTIVITY_ID,
                                   variables
        );

        //complete the CC notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC_ACTIVITY_ID,
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
    void shouldSuccessfullyCompleteNotifyClaimDetails_AndNotifyAppropriateSolicitors_For1v2_whenCalled(
        Boolean twoRespondentRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, "MAIN.CLAIM_DETAILS_NOTIFIED");
        variables.putValue(FLOW_FLAGS, Map.of(
                TWO_RESPONDENT_REPRESENTATIVES, twoRespondentRepresentatives,
                ONE_RESPONDENT_REPRESENTATIVE, !twoRespondentRepresentatives,
                RPA_CONTINUOUS_FEED, true,
                GENERAL_APPLICATION_ENABLED, false
        ));

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
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_ACTIVITY_ID,
            variables
        );

        //complete the CC notification
        ExternalTask notificationCcTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationCcTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC_ACTIVITY_ID,
            variables
        );

        if (twoRespondentRepresentatives) {
            //complete the additional defendant solicitor notification
            ExternalTask secondSolicitorNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(secondSolicitorNotificationTask,
                                       PROCESS_CASE_EVENT,
                                       NOTIFY_RESPONDENT_SOLICITOR_2_CLAIM_DETAILS,
                                       NOTIFY_RESPONDENT_SOLICITOR_2_CLAIM_DETAILS_ACTIVITY_ID,
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
        variables.putValue(FLOW_STATE, "MAIN.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED");
        variables.putValue(FLOW_FLAGS, Map.of(GENERAL_APPLICATION_ENABLED, false));

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
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_ACTIVITY_ID,
            variables
        );

        //complete the CC notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC,
            NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_DETAILS_CC_ACTIVITY_ID,
            variables
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask,
            PROCESS_CASE_EVENT,
            "PROCEEDS_IN_HERITAGE_SYSTEM",
            "ProceedOffline"
        );

        //Notify Applicant Solicitor
        ExternalTask proceedOfflineTask1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask1,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE,
            NOTIFY_CLAIM_DETAILS_OFFLINE_APPLICANT_SOLICITOR1_ACTIVITY_ID
        );

        //Notify RPA
        ExternalTask proceedOfflineTask2 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask2,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_OFFLINE_ACTIVITY_ID
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
