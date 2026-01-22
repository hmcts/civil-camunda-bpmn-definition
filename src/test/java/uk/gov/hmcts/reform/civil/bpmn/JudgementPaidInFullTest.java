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

class JudgementPaidInFullTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "JUDGMENT_PAID_IN_FULL";
    public static final String PROCESS_ID = "JUDGMENT_PAID_IN_FULL";
    private static final String SEND_JUDGMENT_DETAILS_CJES = "SEND_JUDGMENT_DETAILS_CJES";
    private static final String SEND_JUDGMENT_DETAILS_CJES_ACTIVITY_ID = "SendJudgmentDetailsToCJES";

    private static final String NOTIFY_RPA = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ACTIVITY_ID = "NotifyRPA";

    public JudgementPaidInFullTest() {
        super("judgement_paid_in_full.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "false, false",
        "true, true",
    })
    void shouldSuccessfullyCompleteJudgmentPaidInFull_whenCalled(boolean joFlag, boolean isCjesServiceEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            IS_JO_LIVE_FEED_ACTIVE, joFlag,
            IS_CJES_SERVICE_ENABLED, isCjesServiceEnabled
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (isCjesServiceEnabled) {
            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                SEND_JUDGMENT_DETAILS_CJES,
                SEND_JUDGMENT_DETAILS_CJES_ACTIVITY_ID
            );
        }
        //complete the dashboard update
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardTask,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            "GenerateDashboardNotificationsJudgmentPaidInFull"
        );

        if (joFlag) {
            ExternalTask notifyRPA = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyRPA,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA,
                NOTIFY_RPA_ACTIVITY_ID,
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
