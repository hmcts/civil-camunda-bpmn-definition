package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UploadTranslatedSealedFormForLipVsLrTest extends BpmnBaseTest {

    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPApplicantClaimantConfirmToProceed";
    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED
        = "NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED";

    public UploadTranslatedSealedFormForLipVsLrTest() {
        super("upload_translated_defendant_sealed_form.bpmn", "UPLOAD_TRANSLATED_DEFENDANT_SEALED_FORM");
    }

    @ParameterizedTest
    @CsvSource({"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION", "MAIN.COUNTER_CLAIM", "MAIN.FULL_DEFENCE"})
    void shouldRunProcess(String responseType) {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", responseType);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (responseType.equals("MAIN.FULL_ADMISSION") || responseType.equals("MAIN.PART_ADMISSION")
            || responseType.equals("MAIN.COUNTER_CLAIM")) {

            //proceed offline
            ExternalTask fullDefenceResponse = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                fullDefenceResponse,
                PROCESS_CASE_EVENT,
                "PROCEEDS_IN_HERITAGE_SYSTEM",
                "ProceedOffline"
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

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
                "Activity_0ncmkab"
            );

            //complete the notification to LIP applicant
            ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyApplicant,
                PROCESS_CASE_EVENT,
                NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED,
                NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
            );
        }

        if (responseType.equals("MAIN.FULL_DEFENCE")) {
            // continuous feed
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
            );

            ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyApplicant,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE",
                "DefendantResponseFullDefenceNotifyApplicantSolicitor1"
            );
        }
        //complete the notification to LR respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC",
            "Notify",
            variables
        );

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE",
            "GenerateClaimantDashboardNotificationDefendantResponse"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
