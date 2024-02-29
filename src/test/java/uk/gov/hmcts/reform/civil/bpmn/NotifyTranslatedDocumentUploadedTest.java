package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class NotifyTranslatedDocumentUploadedTest extends  BpmnBaseTest {

    private static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT";
    private static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOCUMENT_ID";
    private static final String NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED
        = "NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED";
    private static final String NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED
        = "NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED";

    private static final String NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED_ID
        = "NotifyTranslatedDocumentUploadedToClaimant";

    private static final String NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED_ID
        = "NotifyTranslatedDocumentUploadedToDefendant";

    private static final String UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED = "UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED";
    private static final String UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED_ID = "updateClaimStateAfterTranslateDocumentUploadedID";

    public NotifyTranslatedDocumentUploadedTest() {
        super("upload_translated_document_notify.bpmn", PROCESS_ID);
    }

    @Test
    void shouldNotifyTranslatedDocumentUploaded() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED,
                                   NOTIFY_CLAIMANT_TRANSLATED_DOCUMENT_UPLOADED_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED,
                                   NOTIFY_DEFENDANT_TRANSLATED_DOCUMENT_UPLOADED_ID,
                                   variables
        );

        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED,
                                   UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED_ID,
                                   variables);

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
