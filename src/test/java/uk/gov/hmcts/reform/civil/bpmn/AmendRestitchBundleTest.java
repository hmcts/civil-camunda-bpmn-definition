package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AmendRestitchBundleTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "AMEND_RESTITCH_BUNDLE";
    public static final String PROCESS_ID = "AMEND_RESTITCH_BUNDLE";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_AMEND_RESTITCH_BUNDLE
        = "NOTIFY_CLAIMANT_AMEND_RESTITCH_BUNDLE";
    public static final String NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE
        = "NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE";

    //ACTIVITY IDs
    private static final String NOTIFY_CLAIMANT_AMEND_RESTITCH_BUNDLE_ACTIVITY_ID
        = "NotifyClaimantAmendRestitchBundle";
    private static final String NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE_ACTIVITY_ID
        = "NotifyDefendantAmendRestitchBundle";

    public static final String CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_CLAIMANT
        = "CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_DEFENDANT
        = "CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_DEFENDANT";

    //ACTIVITY IDs
    private static final String CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_CLAIMANT_ACTIVITY_ID
        = "CreateAmendRestitchBundleDashboardNotificationsForClaimant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_DEFENDANT_ACTIVITY_ID
        = "CreateAmendRestitchBundleDashboardNotificationsForDefendant";

    public AmendRestitchBundleTest() {
        super("amend_restitch_bundle.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteAmendRestitchBundle() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

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
                                   NOTIFY_CLAIMANT_AMEND_RESTITCH_BUNDLE,
                                   NOTIFY_CLAIMANT_AMEND_RESTITCH_BUNDLE_ACTIVITY_ID,
                                   variables
        );

        //complete the defendant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE,
                                   NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE_ACTIVITY_ID,
                                   variables
        );

        //complete the claimant dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_CLAIMANT_ACTIVITY_ID,
                                   variables
        );

        //complete the defendant dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_AMEND_RESTITCH_BUNDLE_DEFENDANT_ACTIVITY_ID,
                                   variables
        );



        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
