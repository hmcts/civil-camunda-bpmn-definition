package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AcknowledgeClaimSpecTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "ACKNOWLEDGEMENT_OF_SERVICE";
    private static final String PROCESS_ID = "ACKNOWLEDGE_CLAIM_PROCESS_ID_SPEC";

    private static final String GENERATE_ACK_OF_CLAIM = "GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC";
    private static final String GENERATE_ACK_ACTIVITY_ID = "AcknowledgeClaimGenerateAcknowledgementOfClaimForSpec";
    private static final String NOTIFY_APPLICANT_SOLICITOR = "NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT";
    private static final String NOTIFY_APPLICANT_ACTIVITY_ID = "AcknowledgeClaimNotifyForSpecApplicantSolicitor1";
    private static final String NOTIFY_APPLICANT_CC = "NOTIFY_APPLICANT_SOLICITOR1_FOR_SPEC_CLAIM_ACKNOWLEDGEMENT_CC";
    private static final String NOTIFY_APPLICANT_CC_ACTIVITY_ID = "AcknowledgeClaimNotifyForSpecRespondentSolicitor1CC";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "Activity_1hjuvp5";

    AcknowledgeClaimSpecTest() {
        super("acknowledge_claim_spec.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteAcknowledgeClaimSpec_whenCalled() {
        assertFalse(processInstance.isEnded());

        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_ACK_OF_CLAIM,
            GENERATE_ACK_ACTIVITY_ID
        );

        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR,
            NOTIFY_APPLICANT_ACTIVITY_ID
        );

        ExternalTask applicantNotificationCc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotificationCc,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_CC,
            NOTIFY_APPLICANT_CC_ACTIVITY_ID
        );

        ExternalTask roboticsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            roboticsNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
