package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CourtOfficerOrderTest extends BpmnBaseTest {

    public static final String PROCESS_ID = "COURT_OFFICER_ORDER_ID";

    public static final String MESSAGE_NAME = "COURT_OFFICER_ORDER";
    public static final String CREATE_DASHBOARD_NOTIFICATIONS_COURT_OFFICER_ACTIVITY_ID
        = "GenerateDashboardNotificationsCourtOfficerOrder";
    public static final String NOTIFY_EVENT
        = "NOTIFY_EVENT";
    public static final String NOTIFY_PARTIES_FOR_COURT_OFFICER_ORDER_TASK_ID
        = "GenerateOrderNotifyPartiesCourtOfficerOrder";

    public CourtOfficerOrderTest() {
        super("court_officer_order.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteCourtOfficerOrder(boolean dashboardServiceFlag) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            DASHBOARD_SERVICE_ENABLED, dashboardServiceFlag
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY
        );

        ExternalTask notificationTask;

        //complete all the notifications
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_FOR_COURT_OFFICER_ORDER_TASK_ID,
                                   variables
        );

        if (dashboardServiceFlag) {
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask, PROCESS_CASE_EVENT,
                DASHBOARD_NOTIFICATION_EVENT,
                CREATE_DASHBOARD_NOTIFICATIONS_COURT_OFFICER_ACTIVITY_ID,
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
