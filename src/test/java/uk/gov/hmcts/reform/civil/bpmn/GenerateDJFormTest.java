package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GenerateDJFormTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT";
    public static final String PROCESS_ID = "GENERATE_DJ_FORM";

    public static final String GENERATE_DJ_FORM_EVENT = "GENERATE_DJ_FORM";
    public static final String GENERATE_DJ_FORM_ACTIVITY_ID = "GenerateDJForm";

    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String NOTIFY_ACTIVITY_ID = "GenerateDJFormNotifyParties";

    public GenerateDJFormTest() {
        super("generate_DJ_form.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteGenerateDJFormProcess() {
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

        ExternalTask generateDJFormTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDJFormTask,
            PROCESS_CASE_EVENT,
            GENERATE_DJ_FORM_EVENT,
            GENERATE_DJ_FORM_ACTIVITY_ID,
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
