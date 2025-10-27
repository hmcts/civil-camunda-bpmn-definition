package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotifyClaimTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_DEFENDANT_OF_CLAIM";
    public static final String PROCESS_ID = "NOTIFY_CLAIM";

    //CCD EVENTS
    public static final String NOTIFY_EVENT
        = "NOTIFY_EVENT";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";

    //ACTIVITY IDs
    private static final String NOTIFY_PARTIES
        = "UnspecNotifyClaimNotifier";

    private static final String NOTIFY_APPLICANT_SOLICITOR_1_HAND_OFFLINE_ACTIVITY_ID =
        "ClaimProceedsOfflineUnspecNotifyApplicantSolicitor";

    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID = "ProceedOffline";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

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

    @Test
    void shouldSuccessfullyCompleteNotifyClaim_TakenOffline_GenAppDisabled() {
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
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES,
                                   variables
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(proceedOfflineTask,
                                   PROCESS_CASE_EVENT,
                                   PROCEEDS_IN_HERITAGE_SYSTEM,
                                   PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateApplicationStatus,
            PROCESS_CASE_EVENT,
            TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
            APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimWithApplicationStatus,
            PROCESS_CASE_EVENT,
            APPLICATION_OFFLINE_UPDATE_CLAIM,
            APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //Notify Claimant Solicitor - Handed Offline
        ExternalTask notifyApplicantSolicitorHandedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyApplicantSolicitorHandedOffline,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
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
    void shouldSuccessfullyCompleteNotifyClaim_TakenOffline_GenAppEnabled() {
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
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES,
                                   variables
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(proceedOfflineTask,
                                   PROCESS_CASE_EVENT,
                                   PROCEEDS_IN_HERITAGE_SYSTEM,
                                   PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateApplicationStatus,
                PROCESS_CASE_EVENT,
                TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
                APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimWithApplicationStatus,
                PROCESS_CASE_EVENT,
                APPLICATION_OFFLINE_UPDATE_CLAIM,
                APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //Notify Claimant Solicitor - Handed Offline
        ExternalTask notifyApplicantSolicitorHandedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyApplicantSolicitorHandedOffline,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
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
