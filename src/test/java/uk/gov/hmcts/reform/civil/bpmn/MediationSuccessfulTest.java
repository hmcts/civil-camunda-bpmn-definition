package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class MediationSuccessfulTest extends BpmnBaseTest {
    private static final String FILE_NAME = "mediation_successful.bpmn";
    private static final String MESSAGE_NAME = "MEDIATION_SUCCESSFUL";
    private static final String PROCESS_ID = "MEDIATION_SUCCESSFUL_ID";

    public MediationSuccessfulTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSubmitSuccessfulMediation() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
