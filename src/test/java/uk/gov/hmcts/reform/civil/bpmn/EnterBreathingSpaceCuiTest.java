package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class EnterBreathingSpaceCuiTest extends BpmnBaseTest {

    private static final String FILE_NAME = "enter_breathing_space_cui.bpmn";
    private static final String MESSAGE_NAME = "ENTER_BREATHING_SPACE_CUI";
    private static final String PROCESS_ID = "ENTER_BREATHING_SPACE_PROCESS_ID_CUI";

    public EnterBreathingSpaceCuiTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullySubmitBreathingSpace() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
