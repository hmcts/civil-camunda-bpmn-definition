package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApplyHelpWithHearingFeeTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_HELP_WITH_HEARING_FEE";
    public static final String PROCESS_ID = "APPLY_HELP_WITH_HEARING_FEE";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES
        = "NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES";

    //ACTIVITY IDs
    private static final String NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES_ACTIVITY_ID
        = "NotifyClaimantHwf";

    public ApplyHelpWithHearingFeeTest() {
        super("apply_help_with_hearing_fee.bpmn", PROCESS_ID);
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

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT, NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES,
                                   NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES_ACTIVITY_ID, variables
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
