package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class NotifyGATranslatedUploadedDocumentsTest extends BpmnBaseTest {

    public NotifyGATranslatedUploadedDocumentsTest() {
        super("upload_translated_document_ga_lip_notify.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_GA_LIP_ID");
    }

    @Test
    void shouldNotifyTranslatedDocumentUploaded() {
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
        //complete the applicant notification
        ExternalTask notificationApplicantTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationApplicantTask,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA",
            "NotifyTranslatedDocumentUploadedToApplicantGA"
        );
        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA",
            "NotifyTranslatedDocumentUploadedToRespondentGA"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }
}
