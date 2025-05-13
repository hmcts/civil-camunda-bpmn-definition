package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UploadTranslatedClaimantsRejectsRepaymentPlanDocumentTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_REJECTS_REPAYMENT_PLAN";
    private static final String PROCESS_ID = "UPLOAD_TRANSLATED_CLAIMANTS_DOCUMENT_PROCESS_ID";
    private static final String NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT
        = "NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT";

    private static final String NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT
        = "NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT";
    private static final String NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT_ACTIVITY_ID
        = "ClaimantDisAgreedRepaymentPlanNotifyLip";

    private static final String NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT_ACTIVITY_ID
        = "ClaimantDisAgreeRepaymentPlanNotifyApplicant";

    public UploadTranslatedClaimantsRejectsRepaymentPlanDocumentTest() {
        super(
            "upload_translated_document_claimant_rejects_repayment_plan.bpmn",
            PROCESS_ID
        );
    }

    private void notifyClaimantClaimantRejectRepayment() {
        assertCompletedCaseEvent(NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT, NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT_ACTIVITY_ID);
    }

    private void notifyRespondentClaimantRejectRepayment() {
        assertCompletedCaseEvent(NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT, NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT_ACTIVITY_ID);
    }

    private void assertCompletedCaseEvent(String eventName, String activityId) {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            eventName,
            activityId
        );
    }

    @Test
    void shouldRunProcess_ClaimIsInFullAdmitRejectRepaymentUploadDocuments() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        notifyRespondentClaimantRejectRepayment();
        notifyClaimantClaimantRejectRepayment();
        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
