package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

public class ClaimSettledLiPTest extends BpmnBaseTest {

    private static final String FILE_NAME = "claim_settled_lip.bpmn";
    private static final String MESSAGE_NAME = "LIP_CLAIM_SETTLED";
    private static final String PROCESS_ID = "LIP_CLAIM_SETTLED_PROCESS_ID";

    //CCD Case Event
    private static final String NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM
        = "NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM";

    //Activity IDs
    private static final String NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM_ACTIVITY_ID
        = "NotifyDefendantClaimantSettleTheClaim";

    private static final String CLAIM_SETTLED = "GenerateDashboardNotificationsClaimSettled";

    public ClaimSettledLiPTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldSuccessfullyCompleteClaimSettledLiP(boolean dashboardServiceEnabled) {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled));
        startBusinessProcess(variables);
        notifyRespondentClaimantSettleTheClaim(variables);
        if (dashboardServiceEnabled) {
            generateDashboardNotifications(variables);
        }
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    private void notifyRespondentClaimantSettleTheClaim(VariableMap variables) {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM,
            NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM_ACTIVITY_ID,
            variables
        );
    }

    private void generateDashboardNotifications(VariableMap variables) {
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            CLAIM_SETTLED,
            variables
        );
    }
}
