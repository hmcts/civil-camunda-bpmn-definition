package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProcessCoscApplicationTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "PROCESS_COSC_APPLICATION";
    private static final String PROCESS_ID = "PROCESS_COSC_APPLICATION_PROCESS_ID";
    private static final String SEND_DETAILS_CJES = "sendDetailsToCJES";

    public ProcessCoscApplicationTest() {
        super("process_cosc_application.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true", "false"
    })
    void shouldSuccessfullyCompleteAcknowledgeClaim_whenCalled(boolean cjes) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(SEND_DETAILS_CJES, cjes);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables
        );

        ExternalTask checkMarkPaidInFull = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            checkMarkPaidInFull,
            PROCESS_CASE_EVENT,
            "CHECK_AND_MARK_PAID_IN_FULL",
            "CheckAndMarkDefendantPaidInFull"
        );

        if (cjes) {
            ExternalTask sendJudgement = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendJudgement,
                PROCESS_CASE_EVENT,
                "SEND_JUDGMENT_DETAILS_CJES",
                "SendJudgmentDetailsCJES"
            );
        }


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
