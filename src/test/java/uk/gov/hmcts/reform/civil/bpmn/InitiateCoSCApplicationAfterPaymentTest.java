package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InitiateCoSCApplicationAfterPaymentTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "INITIATE_COSC_APPLICATION_AFTER_PAYMENT";
    public static final String PROCESS_ID = "COSC_INITIATE_AFTER_PAYMENT_PROCESS_ID";
    private static final String CHECK_PAID_IN_FULL_SCHED_DEADLINE = "CHECK_PAID_IN_FULL_SCHED_DEADLINE";
    private static final String CHECK_PAID_IN_FULL_SCHED_DEADLINE_ACTIVITY_ID = "CheckMarkPaidInFullAndAddSchedulerDeadline";

    public InitiateCoSCApplicationAfterPaymentTest() {
        super("initiate_cosc_application_after_payment.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteAddNotes_whenCalled() {
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

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            CHECK_PAID_IN_FULL_SCHED_DEADLINE,
            CHECK_PAID_IN_FULL_SCHED_DEADLINE_ACTIVITY_ID
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
