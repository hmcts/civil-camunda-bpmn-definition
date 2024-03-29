package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AcknowledgeClaimTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "ACKNOWLEDGE_CLAIM";
    private static final String PROCESS_ID = "ACKNOWLEDGE_CLAIM_PROCESS_ID";

    private static final String NOTIFY_APPLICANT_SOLICITOR_1 = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT";
    private static final String GENERATE_ACKNOWLEDGEMENT_OF_CLAIM = "GENERATE_ACKNOWLEDGEMENT_OF_CLAIM";
    private static final String NOTIFICATION_ACTIVITY_ID = "AcknowledgeClaimNotifyApplicantSolicitor1";
    private static final String GENERATE_CERTIFICATE_ACTIVITY_ID = "AcknowledgeClaimGenerateAcknowledgementOfClaim";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";

    public AcknowledgeClaimTest() {
        super("acknowledge_claim.bpmn", "ACKNOWLEDGE_CLAIM_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteAcknowledgeClaim_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY);

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(documentGeneration,
                                   PROCESS_CASE_EVENT,
                                   GENERATE_ACKNOWLEDGEMENT_OF_CLAIM,
                                   GENERATE_CERTIFICATE_ACTIVITY_ID);

        //complete the notification to applicant
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notification,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR_1,
                                   NOTIFICATION_ACTIVITY_ID);

        //complete the CC notification to respondent
        notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC",
                                   "AcknowledgeClaimNotifyRespondentSolicitor1CC"
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
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
