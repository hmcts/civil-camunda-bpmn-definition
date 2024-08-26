package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DismissCaseTest extends BpmnBaseTest {

    public static final String PROCESS_ID = "DISMISS_CASE_ID";

    public DismissCaseTest() {
        super("dismiss_case.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteDismissCase() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("DISMISS_CASE").getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true,
            CASE_PROGRESSION_ENABLED, true
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
                                   "NOTIFY_CLAIMANT_DISMISS_CASE",
                                   "NotifyClaimant",
                                   variables
        );

        //complete the defendant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "NOTIFY_DEFENDANT_DISMISS_CASE",
                                   "NotifyDefendant",
                                   variables
        );

        // clean notifications and block progress
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "DISMISS_CASE_BLOCK_PROGRESS",
                                   "CleanDashboard",
                                   variables
        );

        //complete the claimant dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_CLAIMANT",
                                   "DashboardNotifyClaimant",
                                   variables
        );

        //complete the defendant dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_DEFENDANT",
                                   "DashboardNotifyDefendant",
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
