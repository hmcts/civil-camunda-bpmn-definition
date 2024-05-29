package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestNonDivergentJudgementByAdmissionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_ID";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocClaimant";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocDefendant";

    public RequestNonDivergentJudgementByAdmissionTest() {
        super("judgement_by_admission_non_divergent_spec.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteRequestJudgementByAdmission() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables);

        ExternalTask generateDocClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDocClaimant,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID
        );

        ExternalTask generateDocDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDocDefendant,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID
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
