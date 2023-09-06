package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ClaimantResponseCuiTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "CLAIMANT_RESPONSE_CUI";
    private static final String PROCESS_ID = "CLAIMANT_RESPONSE_CUI_PROCESS_ID_CUI";

    //CCD Case Event

    //ACTIVITY IDs
  public ClaimantResponseCuiTest() {
        super(
            "claimant_response_cui.bpmn",
            "CLAIMANT_RESPONSE_CUI_PROCESS_ID_CUI"
        );
    }


    @Test
    void shouldRunProcess() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

}
