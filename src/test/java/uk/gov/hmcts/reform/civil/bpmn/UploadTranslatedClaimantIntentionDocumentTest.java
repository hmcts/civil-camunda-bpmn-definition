package uk.gov.hmcts.reform.civil.bpmn;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

public class UploadTranslatedClaimantIntentionDocumentTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_LIP";
    private static final String SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT = "SET_SETTLEMENT_AGREEMENT_DEADLINE";
    private static final String SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID = "SetSettlementAgreementDeadline";
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT
        = "NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC";
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPRespondentClaimantConfirmToProceed";
    private static final String UPDATE_CLAIM_STATE_EVENT
        = "UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED";
    private static final String UPDATE_CLAIM_STATE_ACTIVITY_ID
        = "UpdateClaimStateAfterTranslatedDocUploaded";

    public UploadTranslatedClaimantIntentionDocumentTest() {
        super("upload_translated_claimant_intention_document_notify.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION");
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
            SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID
        );

        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );

        //complete the state change task
        ExternalTask updateClaimStateTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            UPDATE_CLAIM_STATE_EVENT,
            UPDATE_CLAIM_STATE_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
