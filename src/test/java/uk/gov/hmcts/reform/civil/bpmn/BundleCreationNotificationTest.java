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

class BundleCreationNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "BUNDLE_CREATION_NOTIFICATION";
    public static final String PROCESS_ID = "BUNDLE_CREATION_NOTIFICATION";
    private static final String BUNDLE_CREATION = "GenerateDashboardNotificationsBundleCreation";

    public BundleCreationNotificationTest() {
        super("bundle_creation_notification.bpmn", "BUNDLE_CREATION_NOTIFICATION");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteBundleCreatedMultiparty(boolean dashboardServiceEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                UNREPRESENTED_DEFENDANT_ONE, false,
                DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the notification for all parties
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_EVENT",
                "BundleCreationNotify"
        );

        if (dashboardServiceEnabled) {
            //complete the Dashboard creation
            ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(dashboardTask,
                PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                BUNDLE_CREATION,
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
