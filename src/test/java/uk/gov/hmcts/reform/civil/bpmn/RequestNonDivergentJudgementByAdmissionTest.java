package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestNonDivergentJudgementByAdmissionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "REQUEST_JUDGEMENT_ADMISSION_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "REQUEST_JUDGEMENT_ADMISSION_NON_DIVERGENT_SPEC_ID";

    //CCD CASE EVENT
    public static final String GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC = "GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC";

    //ACTIVITY IDs
    public static final String GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC_ACTIVITY_ID = "GenerateJudgmentByAdmissonDoc";

    public RequestNonDivergentJudgementByAdmissionTest() {
        super("request_non_divergent_judgement_by_admission.bpmn", PROCESS_ID);
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

        //generate the Judgment By Admission Document
        ExternalTask generateDocTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(generateDocTask,
                                   PROCESS_CASE_EVENT,
                                   GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC,
                                   GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC_ACTIVITY_ID
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
