package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestForReconsiderationCuiDefendantTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT";
    public static final String PROCESS_ID = "REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT";

    //CCD CASE EVENT
    public static final String CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT
        = "CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT";

    //ACTIVITY IDs
    private static final String CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT_ACTIVITY_ID
        = "CreateNotificationRequestForReconsiderationClaimant";

    public RequestForReconsiderationCuiDefendantTest() {
        super("request_for_reconsideration_cui_defendant.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteRequestForReconsideration() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            CASE_PROGRESSION_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT,
                                   CREATE_NOTIFICATION_REQUEST_FOR_RECONSIDERATION_CLAIMANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteRequestForReconsiderationNotCP() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            CASE_PROGRESSION_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
