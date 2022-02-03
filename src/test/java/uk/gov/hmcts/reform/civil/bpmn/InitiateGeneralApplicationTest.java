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

class InitiateGeneralApplicationTest extends BpmnBaseGASpecTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "INITIATE_GENERAL_APPLICATION";
    private static final String PROCESS_ID = "INITIATE_GENERAL_APPLICATION_PROCESS_ID";
    //create general application Case
    private static final String CREATE_GENERAL_APPLICATION_EVENT = "CREATE_GENERAL_APPLICATION_CASE";
    private static final String CREATE_GENERAL_APPLICATION_ID = "CreateGeneralApplicationCase";
    //Link General Application Case to Parent Case
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT
        = "LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE";
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID
        = "LinkGeneralApplicationCaseToParentCase";
    //Assigning of roles
    private static final String ASSIGNIN_OF_ROLES_EVENT = "ASSIGN_GA";
    private static final String ASSIGNIN_OF_ROLES_ID = "AssigningOfRoles";
    //Make PBA Payments
    private static final String MAKE_PBA_PAYMENT_EVENT = "MAKE_PBA_PAYMENT_GASPEC";
    private static final String MAKE_PBA_PAYMENT_ID = "GeneralApplicationMakePayment";
    //Notifying respondents
    private static final String NOTYFYING_RESPONDENTS_EVENT = "NOTIFY_RESPONDENT";
    private static final String GENERAL_APPLICATION_NOTIYFYING_ID = "GeneralApplicationNotifying";

    public InitiateGeneralApplicationTest() {
        super("initiate_general_application.bpmn", "INITIATE_GENERAL_APPLICATION_PROCESS_ID");
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteCreateGeneralApplication_whenCalled(Boolean rpaContinuousFeed) {
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

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(CREATE_APPLICATION_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            CREATE_APPLICATION_CASE_EVENT,
            CREATE_GENERAL_APPLICATION_EVENT,
            CREATE_GENERAL_APPLICATION_ID,
            variables
        );

        //link general application case to parent case
        ExternalTask linkCases = assertNextExternalTask(LINK_APPLICATION_CASE_EVENT);
        assertCompleteExternalTask(
            linkCases,
            LINK_APPLICATION_CASE_EVENT,
            LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT,
            LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID,
            variables
        );

        //assigne of roles
        ExternalTask assigneRoles = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assigneRoles,
            PROCESS_CASE_EVENT,
            ASSIGNIN_OF_ROLES_EVENT,
            ASSIGNIN_OF_ROLES_ID,
            variables
        );

        //make pba payment
        ExternalTask makePbaPayment = assertNextExternalTask(PROCESS_PAYMENT_TOPIC);
        assertCompleteExternalTask(
            makePbaPayment,
            PROCESS_PAYMENT_TOPIC,
            MAKE_PBA_PAYMENT_EVENT,
            MAKE_PBA_PAYMENT_ID,
            variables
        );

        //notify respondents
        ExternalTask notifyRespondents = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondents,
            PROCESS_CASE_EVENT,
            NOTYFYING_RESPONDENTS_EVENT,
            GENERAL_APPLICATION_NOTIYFYING_ID,
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
