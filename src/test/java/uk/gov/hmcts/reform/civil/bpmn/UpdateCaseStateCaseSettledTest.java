package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UpdateCaseStateCaseSettledTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CASE_STATE_CASE_SETTLED";
    public static final String PROCESS_ID = "UPDATE_CASE_STATE_CASE_SETTLED";

    public static final String CASE_STATE_UPDATED_CASE_SETTLED = "CASE_STATE_UPDATED_CASE_SETTLED";
    public static final String CASE_STATE_UPDATED_CASE_SETTLED_ID = "UpdateCaseStateCaseSettled";

    public UpdateCaseStateCaseSettledTest() {
        super("update_case_state_case_settled.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyUpdateCaseSate_caseSettled() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   CASE_STATE_UPDATED_CASE_SETTLED,
                                   CASE_STATE_UPDATED_CASE_SETTLED_ID
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }
}
