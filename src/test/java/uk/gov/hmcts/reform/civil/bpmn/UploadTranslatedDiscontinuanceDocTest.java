package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

public class UploadTranslatedDiscontinuanceDocTest extends BpmnBaseTest {

    public UploadTranslatedDiscontinuanceDocTest() {
        super("upload_translated_discontinuance_doc.bpmn", "UPLOAD_TRANSLATED_DISCONTINUANCE_DOC");
    }

    @Test
    void shouldRunProcess() {
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //send notification to defendant lip
        ExternalTask notifyDefendantLip = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyDefendantLip,
            PROCESS_CASE_EVENT,
            "NOTIFY_DISCONTINUANCE_DEFENDANT1",
            "NotifyDiscontinuancetDefendant1"
        );

        //Post Notice of Discontinuance
        ExternalTask sendDiscontinuancePipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendDiscontinuancePipLetter,
            PROCESS_CASE_EVENT,
            "SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1",
            "PostNoticeOfDiscontinuanceDefendant1LIP"
        );

        //send Dashboard Notification
        ExternalTask generateDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE",
            "CreateDefendantDashboardNotificationsForDiscontinuance"
        );

        //send Notification to claimant
        ExternalTask sendNotificationToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendNotificationToClaimant,
            PROCESS_CASE_EVENT,
            "NOTIFY_DISCONTINUANCE_CLAIMANT1",
            "NotifyDiscontinuanceClaimant"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
