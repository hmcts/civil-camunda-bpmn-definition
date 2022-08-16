package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GeneralApplicationProceedsInHeritageTest extends GeneralAppBpmnBaseTest {

    //BPMN Settings
    private static final String CAMUNDA_FILE_NAME = "general_application_proceeds_in_heritage.bpmn";
    private static final String MESSAGE_NAME = "APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String PROCESS_ID = "APPLICATION_PROCEEDS_IN_HERITAGE";
    //Update Parent Claim
    private static final String UPDATE_PARENT_CLAIM_EVENT_ID = "APPLICATION_OFFLINE_UPDATE_PARENT_CLAIM";
    private static final String UPDATE_PARENT_CLAIM_ACTIVITY_ID = "ApplicationOfflineUpdateParentClaim";

    public GeneralApplicationProceedsInHeritageTest() {
        super(CAMUNDA_FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldUpdateParentClaimWithGeneralApplicationStatus_whenGeneralApplicationClosed() {
        VariableMap variables = Variables.createVariables();

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_GENERAL_APP_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_GENERAL_APP_BUSINESS_TOPIC,
                START_GENERAL_APP_BUSINESS_EVENT,
                START_GENERAL_APP_BUSINESS_ACTIVITY,
                variables
        );

        //update parent claim
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_GA_CASE_EVENT);
        assertCompleteExternalTask(
                applicantNotification,
                APPLICATION_EVENT,
                UPDATE_PARENT_CLAIM_EVENT_ID,
                UPDATE_PARENT_CLAIM_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_GENERAL_APP_BUSINESS_PROCESS);
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
        ExternalTask startBusiness = assertNextExternalTask(START_GENERAL_APP_BUSINESS_TOPIC);
        assertFailExternalTask(
                startBusiness,
                START_GENERAL_APP_BUSINESS_TOPIC,
                START_GENERAL_APP_BUSINESS_EVENT,
                START_GENERAL_APP_BUSINESS_ACTIVITY
        );

        assertNoExternalTasksLeft();
    }

}
