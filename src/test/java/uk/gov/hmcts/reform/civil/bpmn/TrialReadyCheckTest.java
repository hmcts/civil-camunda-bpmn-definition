package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TrialReadyCheckTest extends BpmnBaseTest {
    
    private static final String MESSAGE_NAME = "TRIAL_READY_CHECK";
    private static final String PROCESS_ID = "TRIAL_READY_CHECK_PROCESS_ID";
    private static final String CLAIMANT_DASHBOARD_CHANGES = "CREATE_DASHBOARD_NOTIFICATION_TRIAL_READY_CHECK_CLAIMANT1";
    private static final String CLAIMANT_DASHBOARD_CHANGES_ID = "TrialReadyCheckDashboardNotificationsForClaimant1";
    private static final String DEFENDANT_DASHBOARD_CHANGES = "CREATE_DASHBOARD_NOTIFICATION_TRIAL_READY_CHECK_DEFENDANT1";
    private static final String DEFENDANT_DASHBOARD_CHANGES_ID = "TrialReadyCheckDashboardNotificationsForDefendant1";

    public TrialReadyCheckTest() {
        super("trial_ready_check.bpmn", "TRIAL_READY_CHECK_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteAcknowledgeClaim_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY);

        //complete the claimant dashboard changes
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(documentGeneration,
                                   PROCESS_CASE_EVENT,
                                   CLAIMANT_DASHBOARD_CHANGES,
                                   CLAIMANT_DASHBOARD_CHANGES_ID
        );

        //complete the defendant dashboard changes
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            DEFENDANT_DASHBOARD_CHANGES,
            DEFENDANT_DASHBOARD_CHANGES_ID
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
