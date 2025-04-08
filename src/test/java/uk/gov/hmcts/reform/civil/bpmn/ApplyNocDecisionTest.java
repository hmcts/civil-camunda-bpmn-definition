package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplyNocDecisionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_NOC_DECISION";
    public static final String PROCESS_ID = "APPLY_NOC_DECISION";

    //CCD CASE EVENTS
    private static final String NOTIFY_PARTIES = "NOTIFY_EVENT";
    private static final String UPDATE_CASE_DETAILS_AFTER_NOC = "UPDATE_CASE_DETAILS_AFTER_NOC";

    //ACTIVITY IDs
    private static final String TASK_ID_NOTIFY_SOLICITORS = "ChangeOfRepresentationNotifyParties";
    private static final String TASK_ID_UPDATE_CASE_DETAILS = "UpdateCaseDetailsAfterNoC";
    
    public ApplyNocDecisionTest() {
        super("apply_noc_decision.bpmn", PROCESS_ID);
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

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            UPDATE_CASE_DETAILS_AFTER_NOC,
            TASK_ID_UPDATE_CASE_DETAILS
        );

        //complete the notification to relevant parties
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notification,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_PARTIES,
                                   TASK_ID_NOTIFY_SOLICITORS);

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
