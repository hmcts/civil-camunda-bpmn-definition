package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class StayUpdateRequestedTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "STAY_UPDATE_REQUESTED";
    public static final String PROCESS_ID = "STAY_UPDATE_REQUESTED";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED
        = "NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED";
    public static final String NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED
        = "NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED";
    public static final String NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED
        = "NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED";

    //ACTIVITY IDs
    private static final String NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED_ACTIVITY_ID
        = "NotifyClaimantStayUpdateRequested";
    private static final String NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED_ACTIVITY_ID
        = "NotifyDefendantStayUpdateRequested";
    private static final String NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED_ACTIVITY_ID
        = "NotifyDefendant2StayUpdateRequested";

    public StayUpdateRequestedTest() {
        super("stay_update_requested.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteAmendRestitchBundle() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            TWO_RESPONDENT_REPRESENTATIVES, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables
        );

        ExternalTask notificationTask;

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED,
                                   NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED_ACTIVITY_ID,
                                   variables
        );

        //complete the defendant 2 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED,
                                   NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED_ACTIVITY_ID,
                                   variables
        );

        //complete the defendant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED,
                                   NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
