package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefendantSignSettlementAgreementTest extends BpmnBaseTest {

    private static final String FILE_NAME = "defendant_sign_settlement_agreement.bpmn";
    private static final String MESSAGE_NAME = "DEFENDANT_SIGN_SETTLEMENT_AGREEMENT";
    private static final String PROCESS_ID = "DEFENDANT_SIGN_SETTLEMENT_AGREEMENT_PROCESS_ID";

    public DefendantSignSettlementAgreementTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyProcessDefendantSignAgreement() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

}
