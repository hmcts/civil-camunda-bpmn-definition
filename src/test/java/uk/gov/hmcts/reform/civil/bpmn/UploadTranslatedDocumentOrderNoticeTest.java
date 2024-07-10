package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UploadTranslatedDocumentOrderNoticeTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE";
    public static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE";

    public UploadTranslatedDocumentOrderNoticeTest() {
        super("upload_translated_document_order_notice.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE");
    }

    @Test
    void shouldSuccessfullyploadTranslatedDocumentOrderNotice() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE",
            "NotifyClaimantOfUploadedOrderNotice"
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_UPLOADED_DOCUMENT_ORDER_NOTICE",
            "NotifyDefendantOfUploadedOrderNotice"
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
