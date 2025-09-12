package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BundleCreationNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "BUNDLE_CREATION_NOTIFICATION";
    public static final String PROCESS_ID = "BUNDLE_CREATION_NOTIFICATION";

    public BundleCreationNotificationTest() {
        super("bundle_creation_notification.bpmn", "BUNDLE_CREATION_NOTIFICATION");
    }

    @Test
    void shouldSuccessfullyCompleteBundleCreatedMultiparty() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                UNREPRESENTED_DEFENDANT_ONE, false,
                DASHBOARD_SERVICE_ENABLED, true));

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

        //complete the Dashboard creation for defendant
        ExternalTask defendantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(defendantDashboard,
                PROCESS_CASE_EVENT,
                "CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_DEFENDANT1",
                "CreateBundleCreatedDashboardNotificationsForDefendant1"
        );

        //complete the Dashboard creation for claimant
        ExternalTask applicantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantDashboard,
                PROCESS_CASE_EVENT,
                "CREATE_DASHBOARD_NOTIFICATION_FOR_BUNDLE_CREATED_FOR_CLAIMANT1",
                "CreateBundleCreatedDashboardNotificationsForClaimant1"
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
