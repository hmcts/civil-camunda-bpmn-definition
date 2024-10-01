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
    }
}
