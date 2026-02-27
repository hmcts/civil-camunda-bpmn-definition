package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateDjSdoFormTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "STANDARD_DIRECTION_ORDER_DJ";
    private static final String PROCESS_ID = "GENERATE_DJ_SDO_FORM";

    private static final String NOTIFY_PARTIES_EVENT = "NOTIFY_EVENT";
    private static final String NOTIFY_PARTIES_ACTIVITY_ID = "StandardDirectionOrderDJNotifyParties";
    private static final String NOTIFY_RPA_EVENT = "NOTIFY_RPA_DJ_UNSPEC";
    private static final String NOTIFY_RPA_ACTIVITY_ID = "NotifyRPADJ";

    GenerateDjSdoFormTest() {
        super("generate_DJ_SDO_form.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteGenerateDjSdoForm_whenCalled() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            NOTIFY_PARTIES_EVENT,
            NOTIFY_PARTIES_ACTIVITY_ID
        );

        ExternalTask roboticsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            roboticsNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_EVENT,
            NOTIFY_RPA_ACTIVITY_ID
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
