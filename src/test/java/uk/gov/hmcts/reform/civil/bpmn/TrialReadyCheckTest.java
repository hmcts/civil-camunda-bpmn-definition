package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TrialReadyCheckTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "GENERATE_TRIAL_READY_DOCUMENT_APPLICANT";
    private static final String PROCESS_ID = "GENERATE_TRIAL_READY_DOCUMENT_APPLICANT";
    private static final String GENERATE_TRIAL_READY_DOCUMENT_APPLICANT_EVENT = "GENERATE_TRIAL_READY_DOCUMENT_APPLICANT";
    private static final String GENERATE_TRIAL_READY_DOCUMENT_APPLICANT_ACTIVITY_ID = "GenerateTrialReadyDocumentApplicant";

    TrialReadyCheckTest() {
        super("generate_trial_ready_document_applicant.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyDocument_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the hearing form process
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                GENERATE_TRIAL_READY_DOCUMENT_APPLICANT_EVENT,
                GENERATE_TRIAL_READY_DOCUMENT_APPLICANT_ACTIVITY_ID
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
