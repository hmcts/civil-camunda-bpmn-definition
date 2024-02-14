package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HwFMoreInformationNeededTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED";
    public static final String PROCESS_ID = "NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED";

    //CCD CASE EVENT
    public static final String NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED
        = "NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED";

    //ACTIVITY IDs
    private static final String NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED_ACTIVITY_ID
        = "NotifyClaimantHwfMoreInfoNeeded";

    public HwFMoreInformationNeededTest() {
        super("hwf_more_information_needed.bpmn", PROCESS_ID);
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
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT, NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED,
                                   NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED_ACTIVITY_ID, variables
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
