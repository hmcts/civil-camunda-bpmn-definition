package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TrialReadyNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "TRIAL_READY_NOTIFICATION";
    public static final String PROCESS_ID = "TRIAL_READY_NOTIFICATION";

    public TrialReadyNotificationTest() {
        super("trial_ready_notification.bpmn", "TRIAL_READY_NOTIFICATION");
    }

    @ParameterizedTest
    @CsvSource({"true, false, false", "false, false, false", "false, true, false", "false, false, true"})
    void shouldSuccessfullyCompleteTrialReadyMultiparty(boolean twoRepresentatives,
                                                        boolean defendantLip, boolean defendant2Lip) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, !defendantLip && !defendant2Lip && !twoRepresentatives,
                TWO_RESPONDENT_REPRESENTATIVES, !defendantLip && !defendant2Lip && twoRepresentatives,
                UNREPRESENTED_DEFENDANT_ONE, defendantLip,
                UNREPRESENTED_DEFENDANT_TWO, defendant2Lip,
                DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the notification for respondent 1
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY",
                "TrialReadyNotifyRespondentSolicitor1"
        );

        if (twoRepresentatives || defendant2Lip) {
            //complete the notification for respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondent2Notification,
                    PROCESS_CASE_EVENT,
                    "NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY",
                    "TrialReadyNotifyRespondentSolicitor2"
            );
        }

        //complete the notification for applicant solicitor
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_TRIAL_READY",
                "TrialReadyNotifyApplicantSolicitor1"
        );
        //complete the dashboard notification for Respondent
        ExternalTask respondentDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentDashboardNotification,
                PROCESS_CASE_EVENT,
                "CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_DEFENDANT",
                "GenerateDefendantDashboardNotificationTrialArrangements"
        );
        //complete the dashboard notification for Applicant
        ExternalTask applicantDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantDashboardNotification,
                PROCESS_CASE_EVENT,
                "CREATE_DASHBOARD_NOTIFICATION_CP_TRIAL_ARRANGEMENTS_CLAIMANT",
                "GenerateClaimantDashboardNotificationTrialArrangements"
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
