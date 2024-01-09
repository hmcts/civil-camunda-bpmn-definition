package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UploadTranslatedClaimIssueDocumentTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_LIP";
    private static final String PROCESS_CLAIM_ISSUE_EVENT = "PROCESS_CLAIM_ISSUE_SPEC";
    private static final String PROCESS_CLAIM_ISSUE_ACTIVITY_ID = "IssueClaimForSpecUnrepresentedSolicitor";
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
            = "NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
            = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
            = "NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
            = "NotifyRespondent1";
    public UploadTranslatedClaimIssueDocumentTest() {
        super("upload_translated_claim_issue_document_notify.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_LIP");
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

        //complete the claim issue
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                PROCESS_CLAIM_ISSUE_EVENT,
                PROCESS_CLAIM_ISSUE_ACTIVITY_ID
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
                NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }



}
