package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class StayCaseTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "STAY_CASE";
    public static final String PROCESS_ID = "STAY_CASE";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_STAY_CASE
        = "NOTIFY_CLAIMANT_STAY_CASE";
    public static final String NOTIFY_DEFENDANT_STAY_CASE
        = "NOTIFY_DEFENDANT_STAY_CASE";
    public static final String NOTIFY_DEFENDANT_TWO_STAY_CASE
        = "NOTIFY_DEFENDANT_TWO_STAY_CASE";
    public static final String CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT
        = "CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_DEFENDANT
        = "CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_DEFENDANT";

    //ACTIVITY IDs
    private static final String NOTIFY_CLAIMANT_STAY_CASE_ACTIVITY_ID
        = "NotifyClaimantStayCase";
    private static final String NOTIFY_DEFENDANT_STAY_CASE_ACTIVITY_ID
        = "NotifyDefendantStayCase";
    private static final String NOTIFY_DEFENDANT_TWO_STAY_CASE_ACTIVITY_ID
        = "NotifyDefendant2StayCase";
    private static final String CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT_ACTIVITY_ID
        = "GenerateDashboardNotificationStayCaseClaimant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_DEFENDANT_ACTIVITY_ID
        = "GenerateDashboardNotificationStayCaseDefendant";

    public StayCaseTest() {
        super("stay_case.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteAmendRestitchBundle(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            DASHBOARD_SERVICE_ENABLED, true,
            CASE_PROGRESSION_ENABLED, true,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives
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
                                   NOTIFY_CLAIMANT_STAY_CASE,
                                   NOTIFY_CLAIMANT_STAY_CASE_ACTIVITY_ID,
                                   variables
        );

        if (twoRepresentatives) {
            //complete the defendant 2 notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                       NOTIFY_DEFENDANT_TWO_STAY_CASE,
                                       NOTIFY_DEFENDANT_TWO_STAY_CASE_ACTIVITY_ID,
                                       variables
            );
        }

        //complete the defendant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT_STAY_CASE,
                                   NOTIFY_DEFENDANT_STAY_CASE_ACTIVITY_ID,
                                   variables
        );

        //complete the claimant dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_CLAIMANT_ACTIVITY_ID,
                                   variables
        );

        //complete the defendant dashboard notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_STAY_CASE_DEFENDANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
