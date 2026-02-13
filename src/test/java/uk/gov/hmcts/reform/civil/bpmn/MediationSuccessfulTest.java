package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class MediationSuccessfulTest extends BpmnBaseTest {

    private static final String FILE_NAME = "mediation_successful.bpmn";
    private static final String MESSAGE_NAME = "MEDIATION_SUCCESSFUL";
    private static final String PROCESS_ID = "MEDIATION_SUCCESSFUL_ID";
    private static final String NOTIFY_EVENT
        = "NOTIFY_EVENT";

    private static final String NOTIFY_MEDIATION_SUCCESSFUL_ACTIVITY_ID
        = "MediationSuccessfulNotifyParties";

    public MediationSuccessfulTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSubmitSuccessfulMediationAndGenerateDashboardNotification() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            DASHBOARD_SERVICE_ENABLED, true
        ));
        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_ACTIVITY_ID,
                                   variables
        );

        //dashboardNotification applicant
        ExternalTask dashboardApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardApplicant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_SUCCESSFUL_FOR_APPLICANT",
            "GenerateDashboardNotificationClaimantMediationSuccessful"
        );

        //dashboardNotification respondent
        ExternalTask dashboardRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardRespondent,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_SUCCESSFUL_FOR_RESPONDENT",
            "GenerateDashboardNotificationDefendantMediationSuccessful"
        );

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    @Test
    void shouldSubmitSuccessfulMediationAndNoGenerateDashboardNotification() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
                DASHBOARD_SERVICE_ENABLED, false
        ));
        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_ACTIVITY_ID,
                                   variables
        );

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
