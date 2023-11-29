package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RespondToDirectionsGeneralApplicationTest extends BpmnBaseGAAfterPaymentTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "RESPOND_TO_JUDGE_DIRECTIONS";
    private static final String PROCESS_ID = "RESPOND_TO_DIRECTIONS_GA_PROCESS_ID";
    public static final String UPDATE_FROM_GA_CASE_EVENT = "updateFromGACaseEvent";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDraftDocToMainCaseID";
    private static final String WAIT_PDF_UPDATE_ID = "WaitCivilDraftDocumentUpdatedId";
    private static final String WAIT_PDF_UPDATE_TOPIC = "WAIT_CIVIL_DOC_UPDATED_GASPEC";
    private static final String WAIT_PDF_UPDATE_EVENT = "WAIT_GA_DRAFT";

    public RespondToDirectionsGeneralApplicationTest() {
        super("respond_to_directions_general_application.bpmn",
              "RESPOND_TO_DIRECTIONS_GA_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteRespondToApplication_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //Complete add pdf to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID
        );

        //Complete add pdf to main case event
        ExternalTask waitMainCaseDocUpdated = assertNextExternalTask(WAIT_PDF_UPDATE_TOPIC);
        assertCompleteExternalTask(
                waitMainCaseDocUpdated,
                WAIT_PDF_UPDATE_TOPIC,
                WAIT_PDF_UPDATE_EVENT,
                WAIT_PDF_UPDATE_ID
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
