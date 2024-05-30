package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TransferOnlineCaseInGATest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "TRIGGER_TASK_RECONFIG_GA";
    public static final String PROCESS_ID = "GA_TRIGGER_TASK_RECONFIG";
    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_TASK_RECONFIG_GA";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";

    public TransferOnlineCaseInGATest() {
        super("transfer_online_case_in_general_application.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCallGAWhenGeneralApplicationEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotCallGAWhenGeneralApplicationDisabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

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
