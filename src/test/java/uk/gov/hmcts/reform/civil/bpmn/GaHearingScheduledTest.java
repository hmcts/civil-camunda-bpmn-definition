package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GaHearingScheduledTest extends BpmnBaseHearingScheduledGATest {

    private static final String MESSAGE_NAME = "HEARING_SCHEDULED_GA";
    private static final String PROCESS_ID = "GA_HEARING_SCHEDULED_PROCESS_ID";

    private static final String GENERATE_HEARING_NOTICE_EVENT = "GENERATE_HEARING_NOTICE_DOCUMENT";
    private static final String GENERATE_HEARING_FORM_ACTIVITY_ID = "GenerateHearingNoticeDocument";

    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "LinkDocumentToParentCase";

    private static final String NOTIFY_HEARING_NOTICE_CLAIMANT_EVENT = "NOTIFY_HEARING_NOTICE_CLAIMANT";
    private static final String NOTIFY_HEARING_NOTICE_CLAIMANT_ACTIVITY_ID = "NotifyHearingNoticeClaimant";
    private static final String NOTIFY_HEARING_NOTICE_DEFENDANT_EVENT = "NOTIFY_HEARING_NOTICE_DEFENDANT";
    private static final String NOTIFY_HEARING_NOTICE_DEFENDANT_ACTIVITY_ID = "NotifyHearingNoticeDefendant";

    public GaHearingScheduledTest() {
        super("ga_hearing_scheduled_access.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteCreatePDFDocument_whenCalled() {
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
        //Generate Hearing Notice Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_HEARING_NOTICE_EVENT,
            GENERATE_HEARING_FORM_ACTIVITY_ID
        );

        //Link Document to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID
        );

        //Notify Hearing Notice Claimant
        ExternalTask notifyHearingNoticeClaimant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeClaimant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_ACTIVITY_ID
        );

        //Notify Hearing Notice Defendant(s)
        ExternalTask notifyHearingNoticeDefendant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeDefendant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_ACTIVITY_ID
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
