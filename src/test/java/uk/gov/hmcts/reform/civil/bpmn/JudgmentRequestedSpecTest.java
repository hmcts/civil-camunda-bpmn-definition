package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JudgmentRequestedSpecTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "JUDGMENT_REQUESTED_SPEC";
    public static final String PROCESS_ID = "JUDGMENT_REQUESTED_SPEC";
    public static final String NOTIFY_ACTIVITY_ID = "DJ_NON_DIVERGENT_NOTIFIER";
    public static final String DASHBOARD_NOTIFICATION_ACTIVITY_ID = "GenerateDashboardNotificationsDJNonDivergent";

    public JudgmentRequestedSpecTest() {
        super("judgment_requested_spec.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteJudgmentRequestedSpecProcess() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Variables.createVariables());

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notifyPartiesTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyPartiesTask,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            NOTIFY_ACTIVITY_ID,
            variables
        );

        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            DASHBOARD_NOTIFICATION_ACTIVITY_ID,
            variables
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
