package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManageContactInformationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "MANAGE_CONTACT_INFORMATION";
    public static final String PROCESS_ID = "MANAGE_CONTACT_INFORMATION_ID";
    public static final String PROCESS_EVENT_WITH_DESCRIPTION = "processEventWithDescription";

    //CCD CASE EVENT
    public static final String CONTACT_INFORMATION_UPDATED = "CONTACT_INFORMATION_UPDATED";
    public static final String CONTACT_INFORMATION_UPDATED_ACTIVITY_ID = "ContactInformationUpdated";
    public static final String CONTACT_INFORMATION_UPDATED_WA = "CONTACT_INFORMATION_UPDATED_WA";
    public static final String CONTACT_INFORMATION_UPDATED_WA_ACTIVITY_ID = "ContactInformationUpdatedWa";

    public ManageContactInformationTest() {
        super("manage_contact_information.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteMAnageContactInformation_whenCalled(boolean submittedByCaseworker) {

        assertFalse(processInstance.isEnded());

        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("submittedByCaseworker", submittedByCaseworker);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask contactInformationUpdatedTask = assertNextExternalTask(PROCESS_EVENT_WITH_DESCRIPTION);
        assertCompleteExternalTask(
            contactInformationUpdatedTask,
            PROCESS_EVENT_WITH_DESCRIPTION,
            CONTACT_INFORMATION_UPDATED,
            CONTACT_INFORMATION_UPDATED_ACTIVITY_ID,
            variables
        );

        if (!submittedByCaseworker) {
            ExternalTask waTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                waTask,
                PROCESS_CASE_EVENT,
                CONTACT_INFORMATION_UPDATED_WA,
                CONTACT_INFORMATION_UPDATED_WA_ACTIVITY_ID,
                variables
            );
        }

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
