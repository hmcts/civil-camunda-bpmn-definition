package uk.gov.hmcts.reform.civil.bpmn;

import java.util.Map;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RespondentResponseGeneralApplicationTest extends BpmnBaseGAAfterPaymentTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "RESPOND_TO_APPLICATION";
    private static final String PROCESS_ID = "GA_RESPONDENT_RESPONSE_PROCESS_ID";
    public static final String APPLICATION_PROCESS_CASE_EVENT = "applicationProcessCaseEventGASpec";
    private static final String GENERATE_DRAFT_DOCUMENT = "GENERATE_DRAFT_DOCUMENT";
    private static final String GENERATE_DRAFT_DOCUMENT_ID = "GenerateDraftDocumentId";
    public static final String UPDATE_FROM_GA_CASE_EVENT = "updateFromGACaseEvent";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDraftDocToMainCaseID";
    private static final String WAIT_PDF_UPDATE_ID = "WaitCivilDraftDocumentUpdatedId";
    private static final String WAIT_PDF_UPDATE_TOPIC = "WAIT_CIVIL_DOC_UPDATED_GASPEC";
    private static final String WAIT_PDF_UPDATE_EVENT = "WAIT_GA_DRAFT";
    private static final String TRIGGER_MAIN_CASE_ID = "TriggerMainCaseToMoveOfflineId";
    private static final String TRIGGER_MAIN_CASE_TOPIC = "processGaCaseEvent";
    private static final String TRIGGER_MAIN_CASE_EVENT = "TRIGGER_MAIN_CASE_FROM_GA";
    private static final String VARY_JUDGE_GA_BY_RESP = "VARY_JUDGE_GA_BY_RESP";
    private static final String WELSH_ENABLED = "WELSH_ENABLED";

    public RespondentResponseGeneralApplicationTest() {
        super("respondent_response_general_application.bpmn",
              "GA_RESPONDENT_RESPONSE_PROCESS_ID");
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void shouldSuccessfullyCompleteRespondToApplication_whenCalled(boolean isVaryJudgementAppTakenOffline) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            VARY_JUDGE_GA_BY_RESP, isVaryJudgementAppTakenOffline,
            WELSH_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_DRAFT_DOCUMENT,
            GENERATE_DRAFT_DOCUMENT_ID,
            variables
        );

        //Complete add pdf to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Complete add pdf to main case event
        ExternalTask waitMainCaseDocUpdated = assertNextExternalTask(WAIT_PDF_UPDATE_TOPIC);
        assertCompleteExternalTask(
                waitMainCaseDocUpdated,
                WAIT_PDF_UPDATE_TOPIC,
                WAIT_PDF_UPDATE_EVENT,
                WAIT_PDF_UPDATE_ID,
                variables
        );

        if (isVaryJudgementAppTakenOffline) {
            ExternalTask triggerMainCase = assertNextExternalTask(TRIGGER_MAIN_CASE_TOPIC);
            assertCompleteExternalTask(
                triggerMainCase,
                TRIGGER_MAIN_CASE_TOPIC,
                TRIGGER_MAIN_CASE_EVENT,
                TRIGGER_MAIN_CASE_ID,
                variables
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
