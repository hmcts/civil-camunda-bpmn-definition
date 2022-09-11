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

class ClaimDismissedTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DISMISS_CLAIM";
    public static final String PROCESS_ID = "DISMISS_CLAIM";

    public static final String TRIGGER_APPLICATION_CLOSURE = "TRIGGER_APPLICATION_CLOSURE";
    private static final String APPLICATION_CLOSURE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_CLOSED_UPDATE_CLAIM = "APPLICATION_CLOSED_UPDATE_CLAIM";
    private static final String APPLICATION_CLOSED_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

    public ClaimDismissedTest() {
        super("claim_dismissed.bpmn", "DISMISS_CLAIM");
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenPastClaimNotificationDeadline() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE");
        variables.putValue("flowFlags", Map.of("GENERAL_APPLICATION_ENABLED", false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenPastClaimDetailsNotificationDeadline() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE");
        variables.putValue("flowFlags", Map.of("GENERAL_APPLICATION_ENABLED", false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldNotifyAllParties_whenPastClaimDismissedDeadline(boolean has2RespondentSolicitors) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE");
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !has2RespondentSolicitors,
            TWO_RESPONDENT_REPRESENTATIVES, has2RespondentSolicitors,
            GENERAL_APPLICATION_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            rpaNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
            "NotifyRoboticsOnCaseHandedOffline"
        );

        //complete the notification to respondent
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyRespondentSolicitor1"
        );

        if (has2RespondentSolicitors) {
            //complete the notification to respondent 2
            ExternalTask respondentTwoNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondentTwoNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED",
                "ClaimDismissedNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenPastClaimNotificationDeadline_GAEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE");
        variables.putValue("flowFlags", Map.of("GENERAL_APPLICATION_ENABLED", true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateApplicationStatus,
                PROCESS_CASE_EVENT,
                TRIGGER_APPLICATION_CLOSURE,
                APPLICATION_CLOSURE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimWithApplicationStatus,
                PROCESS_CASE_EVENT,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                APPLICATION_CLOSED_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenPastClaimDetailsNotificationDeadline_GAEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE");
        variables.putValue("flowFlags", Map.of("GENERAL_APPLICATION_ENABLED", true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateApplicationStatus,
                PROCESS_CASE_EVENT,
                TRIGGER_APPLICATION_CLOSURE,
                APPLICATION_CLOSURE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimWithApplicationStatus,
                PROCESS_CASE_EVENT,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                APPLICATION_CLOSED_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldNotifyAllParties_whenPastClaimDismissedDeadline_GAEnabled(boolean has2RespondentSolicitors) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE");
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !has2RespondentSolicitors,
            TWO_RESPONDENT_REPRESENTATIVES, has2RespondentSolicitors,
            GENERAL_APPLICATION_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateApplicationStatus,
                PROCESS_CASE_EVENT,
                TRIGGER_APPLICATION_CLOSURE,
                APPLICATION_CLOSURE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimWithApplicationStatus,
                PROCESS_CASE_EVENT,
                APPLICATION_CLOSED_UPDATE_CLAIM,
                APPLICATION_CLOSED_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            rpaNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
            "NotifyRoboticsOnCaseHandedOffline"
        );

        //complete the notification to respondent
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyRespondentSolicitor1"
        );

        if (has2RespondentSolicitors) {
            //complete the notification to respondent 2
            ExternalTask respondentTwoNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondentTwoNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED",
                "ClaimDismissedNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
            "ClaimDismissedNotifyApplicantSolicitor1"
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
