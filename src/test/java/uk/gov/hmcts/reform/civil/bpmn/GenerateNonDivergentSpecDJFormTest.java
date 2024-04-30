package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateNonDivergentSpecDJFormTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "GENERATE_DJ_NON_DIVERGENT_FORM_SPEC";

    //CCD CASE EVENT
    public static final String GENERATE_DJ_FORM_SPEC = "GENERATE_DJ_FORM_SPEC";

    //ACTIVITY IDs
    public static final String GENERATE_DJ_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormSpec";

    public GenerateNonDivergentSpecDJFormTest() {
        super("generate_non_divergent_spec_DJ_form.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyComplete() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        ExternalTask notificationTask;

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_DJ_FORM_SPEC,
                                   GENERATE_DJ_FORM_SPEC_ACTIVITY_ID
        );

        //end business process
        /*ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();*/
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
