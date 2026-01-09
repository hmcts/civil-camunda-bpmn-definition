package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DecisionOutcomeTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "MOVE_TO_DECISION_OUTCOME";
    public static final String PROCESS_ID = "MOVE_TO_DECISION_OUTCOME";

    //CCD CASE EVENT

    //ACTIVITY IDs
    private static final String UPDATE_DASHBOARD_TASK_LIST_ID
        = "GenerateDashboardNotificationsDecisionOutcome";

    public DecisionOutcomeTest() {
        super("decision_outcome.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteProcess() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the definition outcome for form generation
        ExternalTask defendantTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(defendantTask, PROCESS_CASE_EVENT, DASHBOARD_NOTIFICATION_EVENT,
                                   UPDATE_DASHBOARD_TASK_LIST_ID, variables
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
