package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateDjSdoFormTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "STANDARD_DIRECTION_ORDER_DJ";
    private static final String PROCESS_ID = "GENERATE_DJ_SDO_FORM";

    private static final String NOTIFY_CLAIMANT_EVENT = "NOTIFY_DIRECTION_ORDER_DJ_CLAIMANT";
    private static final String NOTIFY_CLAIMANT_ACTIVITY_ID = "Activity_0nyrqab";
    private static final String NOTIFY_DEFENDANT_EVENT = "NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT";
    private static final String NOTIFY_DEFENDANT_ACTIVITY_ID = "Activity_0txb7dk";
    private static final String NOTIFY_DEFENDANT2_EVENT = "NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2";
    private static final String NOTIFY_DEFENDANT2_ACTIVITY_ID = "Activity_0v7eexn";
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

        ExternalTask claimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_CLAIMANT_EVENT,
            NOTIFY_CLAIMANT_ACTIVITY_ID
        );

        ExternalTask defendantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_DEFENDANT_EVENT,
            NOTIFY_DEFENDANT_ACTIVITY_ID
        );

        ExternalTask defendantTwoNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantTwoNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_DEFENDANT2_EVENT,
            NOTIFY_DEFENDANT2_ACTIVITY_ID
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
