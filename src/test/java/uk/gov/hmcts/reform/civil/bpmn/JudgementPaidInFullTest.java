package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JudgementPaidInFullTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "JUDGMENT_PAID_IN_FULL";
    public static final String PROCESS_ID = "JUDGMENT_PAID_IN_FULL";
    private static final String SEND_JUDGMENT_DETAILS_CJES = "SEND_JUDGMENT_DETAILS_CJES";
    private static final String SEND_JUDGMENT_DETAILS_CJES_ACTIVITY_ID = "SendJudgmentDetailsToCJES";

    private static final String UPDATE_CLAIMANT_DASHBOARD = "UPDATE_DASHBOARD_NOTIFICATIONS_JUDGMENT_PAID_CLAIMANT";
    private static final String UPDATE_CLAIMANT_DASHBOARD_ACTIVITY_ID = "UpdateJudgmentPaidDashboardNotificationsClaimant";
    private static final String UPDATE_DEFENDANT_DASHBOARD = "UPDATE_DASHBOARD_NOTIFICATIONS_JUDGMENT_PAID_DEFENDANT";
    private static final String UPDATE_DEFENDANT_DASHBOARD_ACTIVITY_ID = "UpdateJudgmentPaidDashboardNotificationsDefendant";

    public JudgementPaidInFullTest() {
        super("judgement_paid_in_full.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteJudgmentPaidInFull_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            SEND_JUDGMENT_DETAILS_CJES,
            SEND_JUDGMENT_DETAILS_CJES_ACTIVITY_ID
        );

        //complete the claimant dashboard update
        ExternalTask claimantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDashboard,
            PROCESS_CASE_EVENT,
            UPDATE_CLAIMANT_DASHBOARD,
            UPDATE_CLAIMANT_DASHBOARD_ACTIVITY_ID
        );

        //complete the defendant dashboard update
        ExternalTask defendantDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantDashboard,
            PROCESS_CASE_EVENT,
            UPDATE_DEFENDANT_DASHBOARD,
            UPDATE_DEFENDANT_DASHBOARD_ACTIVITY_ID
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
