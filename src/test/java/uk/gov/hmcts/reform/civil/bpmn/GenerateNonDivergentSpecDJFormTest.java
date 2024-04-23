package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateNonDivergentSpecDJFormTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC";

    //CCD CASE EVENT
    public static final String GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT = "GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT";
    public static final String GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT = "GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT";

    //ACTIVITY IDs
    public static final String GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecClaimant";
    public static final String GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecDefendant";

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

        ExternalTask docmosisTask;

        //complete generate dj form claimant spec activity
        docmosisTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(docmosisTask, PROCESS_CASE_EVENT,
                                   GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT,
                                   GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID
        );
        //complete generate dj form claimant spec activity
        docmosisTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(docmosisTask, PROCESS_CASE_EVENT,
                                   GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT,
                                   GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID
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
