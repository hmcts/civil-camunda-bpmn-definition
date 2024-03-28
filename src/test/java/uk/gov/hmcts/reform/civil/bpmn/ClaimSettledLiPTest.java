package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

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

    private static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_CLAIMANT1
        = "CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_CLAIMANT1";

    //Activity IDs
    private static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_CLAIMANT1_ACTIVITY_ID
        = "CreateClaimSettledDashboardNotificationsForClaimant1";

    public ClaimSettledLiPTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    public void shouldSuccessfullyCompleteClaimSettledLiP() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        notifyRespondentClaimantSettleTheClaim();
        createDashboardNotificationForClaimant();
        createDashboardNotificationForDefendant();
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    private void notifyRespondentClaimantSettleTheClaim() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM,
            NOTIFY_DEFENDANT_CLAIMANT_SETTLE_THE_CLAIM_ACTIVITY_ID
        );
    }

    private void createDashboardNotificationForClaimant() {
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_CLAIMANT1,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_CLAIMANT1_ACTIVITY_ID
        );
    }
    
    private void createDashboardNotificationForDefendant() {
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_FOR_CLAIM_SETTLED_FOR_DEFENDANT1",
            "CreateClaimSettledDashboardNotificationsForDefendant1"
        );
    }
}
