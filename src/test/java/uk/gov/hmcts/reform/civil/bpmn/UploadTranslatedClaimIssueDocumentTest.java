package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UploadTranslatedClaimIssueDocumentTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_LIP";
    private static final String PROCESS_CLAIM_ISSUE_EVENT = "PROCESS_CLAIM_ISSUE_SPEC";
    private static final String PROCESS_CLAIM_ISSUE_ACTIVITY_ID = "IssueClaimForLip";
    public static final String SET_LIP_RESPONDENT_RESPONSE_DEADLINE_EVENT = "SET_LIP_RESPONDENT_RESPONSE_DEADLINE";
    private static final String SET_LIP_RESPONDENT_RESPONSE_DEADLINE_ACTIVITY_ID = "Respondent1Deadline";
    private static final String UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED
            = "UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED";
    private static final String UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED_ID
            = "updateClaimStateAfterTranslateDocumentUploadedID";
    public static final String CLAIM_CONTINUING_ONLINE_SPEC_NOTIFIER = "ContinuingClaimOnlineSpecClaimNotifier";
    //notify RPA
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";

    public UploadTranslatedClaimIssueDocumentTest() {
        super("upload_translated_document_claim_issue_notify.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_LIP_ID");
    }

    @Test
    void shouldRunProcess() {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY
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
                PROCESS_CLAIM_ISSUE_ACTIVITY_ID
        );

        //complete the relevant parties notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                CLAIM_CONTINUING_ONLINE_SPEC_NOTIFIER
        );

        //complete the case state update
        ExternalTask notificationTaskForCaseStateUpdate = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTaskForCaseStateUpdate,
                PROCESS_CASE_EVENT,
                UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED,
                UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED_ID
        );

        //complete the case state update
        ExternalTask notificationTaskForDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTaskForDashboard,
                                   PROCESS_CASE_EVENT,
                                   "CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_APPLICANT1",
                                   "CreateIssueClaimDashboardNotificationsForApplicant1"
        );

        //complete the case state update
        ExternalTask notificationTaskForRespondentDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTaskForRespondentDashboard,
                                   PROCESS_CASE_EVENT,
                                   "CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_ISSUE_FOR_RESPONDENT1",
                                   "CreateIssueClaimDashboardNotificationsForDefendant1"
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
