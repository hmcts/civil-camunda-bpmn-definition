package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class NotifyClaimantHWFTest extends BpmnBaseTest {

    private static final String FILE_NAME = "notify_claimant_hwf_outcome.bpmn";
    private static final String MESSAGE_NAME = "NOTIFY_LIP_CLAIMANT_HWF_OUTCOME";
    private static final String PROCESS_ID = "NOTIFY_LIP_CLAIMANT_HWF_OUTCOME";

    public NotifyClaimantHWFTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldNotifyClaimantWithHwfOutcome() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);

        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_LIP_CLAIMANT_HWF_OUTCOME",
            "NotifyClaimantHwFOutcome"
        );

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
