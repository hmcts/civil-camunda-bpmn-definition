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

class CreateGeneralApplicationTest extends  BpmnBaseTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "CREATE_GENERAL_APPLICATION";
    private static final String PROCESS_ID = "CREATE_GENERAL_APPLICATION_PROCESS_ID";
    //create general application Case
    private static final String CREATE_GENERAL_APPLICATION_EVENT = "CREATE_GENERAL_APPLICATION_CASE";
    private static final String CREATE_GENERAL_APPLICATION_ID = "CreateGeneralApplicationCase";
    //Link General Application Case to Parent Case
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT = "LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE";
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID = "LinkGeneralApplicationCaseToParentCase";

    public CreateGeneralApplicationTest() {super("create_general_application.bpmn", "CREATE_GENERAL_APPLICATION_PROCESS_ID");}

    @ParameterizedTest
    @ValueSource(strings ={"true", "false"})
    void shouldSuccessfullyCompleteCreateGeneralApplication_whenCalled(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of("RPA_CONTINUOUS_FEED", rpaContinuousFeed));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(documentGeneration,
                                   PROCESS_CASE_EVENT,
                                   CREATE_GENERAL_APPLICATION_EVENT,
                                   CREATE_GENERAL_APPLICATION_ID,
                                   variables);

        //complete the document generation
        ExternalTask linkCases = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(linkCases,
                                   PROCESS_CASE_EVENT,
                                   LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT,
                                   LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID,
                                   variables);

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
