package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RespondToQueryTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "queryManagementRespondQuery";
    public static final String PROCESS_ID = "queryManagementRespondQuery";
    private static final String GENERATE_QUERY_DOCUMENT = "GENERATE_QUERY_DOCUMENT";
    private static final String GENERATE_QUERY_DOCUMENT_ACTIVITY_ID = "GenerateQueryDocument";
    private static final String DELETE_QUERY_DOCUMENT = "DELETE_QUERY_DOCUMENT";
    private static final String DELETE_QUERY_DOCUMENT_ACTIVITY_ID = "DeleteQueryDocument";
    private static final String NOTIFY_LR = "NOTIFY_RESPONSE_TO_QUERY";
    private static final String NOTIFY_LR_ACTIVITY_ID = "QueryResponseNotify";

    public RespondToQueryTest() {
        super("respond_to_query.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,false", "false,true"})
    void shouldSuccessfullyCompleteRespondToQueryProcess_whenCalled(boolean lipClaim, boolean removeDocument) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, lipClaim));
        variables.put("removeDocument", removeDocument);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (!lipClaim) {
            //generate the query document
            ExternalTask generateQueryDocumentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                generateQueryDocumentTask,
                PROCESS_CASE_EVENT,
                GENERATE_QUERY_DOCUMENT,
                GENERATE_QUERY_DOCUMENT_ACTIVITY_ID
            );

            if (removeDocument) {
                //delete the query document
                ExternalTask deleteQueryDocumentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    deleteQueryDocumentTask,
                    PROCESS_CASE_EVENT,
                    DELETE_QUERY_DOCUMENT,
                    DELETE_QUERY_DOCUMENT_ACTIVITY_ID
                );
            }
        }

        //complete the email notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_LR,
            NOTIFY_LR_ACTIVITY_ID
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
