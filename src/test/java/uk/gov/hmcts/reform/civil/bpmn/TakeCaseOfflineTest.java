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

class TakeCaseOfflineTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "TAKE_CASE_OFFLINE";
    public static final String PROCESS_ID = "TAKE_CASE_OFFLINE";

    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

    public TakeCaseOfflineTest() {
        super("take_case_offline.bpmn", "TAKE_CASE_OFFLINE");
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteTakeCaseOfflineMultiparty(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, false,
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
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                   "TakeCaseOfflineNotifyRespondentSolicitor1"
        );

        if (twoRepresentatives) {
            //complete the notification to respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondent2Notification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE",
                                       "TakeCaseOfflineNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                   "TakeCaseOfflineNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true, true", "true, false", "false, true", "true, null"})
    void shouldSuccessfullyCompleteTakeCaseOfflineUnrepresentedDefendant(boolean unrepresentedDefendant1,
                                                                         boolean unrepresentedDefendant2) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendant1,
            UNREPRESENTED_DEFENDANT_TWO, unrepresentedDefendant2,
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

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            rpaNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //complete the notification to respondent 1
        if (!unrepresentedDefendant1) {
            ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondentNotification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                       "TakeCaseOfflineNotifyRespondentSolicitor1"
            );
        }

        if (!unrepresentedDefendant2) {
            //complete the notification to respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondent2Notification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE",
                                       "TakeCaseOfflineNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                   "TakeCaseOfflineNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteTakeCaseOfflineMultiparty_GAEnabled(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, false,
            GENERAL_APPLICATION_ENABLED, true,
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

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            rpaNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                   "TakeCaseOfflineNotifyRespondentSolicitor1"
        );

        if (twoRepresentatives) {
            //complete the notification to respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondent2Notification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE",
                                       "TakeCaseOfflineNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                   "TakeCaseOfflineNotifyApplicantSolicitor1"
        );

        //Dashboard notification
        ExternalTask mainCaseNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            mainCaseNotification,
            PROCESS_CASE_EVENT,
            "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE",
            "GenerateClaimantDashboardNotificationCaseProceedOffline"
        );

        mainCaseNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            mainCaseNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE",
            "GenerateDefendantDashboardNotificationCaseProceedOffline"
        );

        ExternalTask claimantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT",
            "claimantLipApplicationOfflineDashboardNotification"
        );

        ExternalTask defendantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT",
            "defendantLipApplicationOfflineDashboardNotification"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true, true", "true, false", "false, true", "true, null"})
    void shouldSuccessfullyCompleteTakeCaseOfflineUnrepresentedDefendant_GAEnabled(boolean unrepresentedDefendant1,
                                                                                   boolean unrepresentedDefendant2) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendant1,
            UNREPRESENTED_DEFENDANT_TWO, unrepresentedDefendant2,
            GENERAL_APPLICATION_ENABLED, true,
            DASHBOARD_SERVICE_ENABLED, true
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

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            rpaNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //complete the notification to respondent 1
        if (!unrepresentedDefendant1) {
            ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondentNotification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                       "TakeCaseOfflineNotifyRespondentSolicitor1"
            );
        }

        if (!unrepresentedDefendant2) {
            //complete the notification to respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondent2Notification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE",
                                       "TakeCaseOfflineNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE",
                                   "TakeCaseOfflineNotifyApplicantSolicitor1"
        );

        //Dashboard notification
        ExternalTask mainCaseNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            mainCaseNotification,
            PROCESS_CASE_EVENT,
            "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE",
            "GenerateClaimantDashboardNotificationCaseProceedOffline"
        );

        mainCaseNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            mainCaseNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CASE_PROCEED_OFFLINE",
            "GenerateDefendantDashboardNotificationCaseProceedOffline"
        );

        ExternalTask claimantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT",
            "claimantLipApplicationOfflineDashboardNotification"
        );

        ExternalTask defendantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT",
            "defendantLipApplicationOfflineDashboardNotification"
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
