package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JudgeMakesDecisionGeneralApplicationTest extends BpmnBaseJudgeGASpecTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "JUDGE_MAKES_DECISION";
    private static final String PROCESS_ID = "JUDGE_MAKES_DECISION_PROCESS_ID";
    //Create PDF
    private static final String CREATE_PDF_EVENT = "GENERATE_JUDGES_FORM";
    private static final String CREATE_PDF_ID = "CreatePDFDocument";
    //Add PDF document to main case
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AssPDFDocumentToMainCase";

    public JudgeMakesDecisionGeneralApplicationTest() {
        super("judge_makes_decision_general_application.bpmn", "JUDGE_MAKES_DECISION_PROCESS_ID");
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteCreatePDFDocument_whenCalled(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of("RPA_CONTINUOUS_FEED", rpaContinuousFeed));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete judicial notification process
        ExternalTask judicialNotificationProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            judicialNotificationProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            START_NOTIFICATION_PROCESS_MAKE_DECISION,
            START_NOTIFICATION_PROCESS_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            MAKE_DECISION_CASE_EVENT,
            CREATE_PDF_EVENT,
            CREATE_PDF_ID,
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
