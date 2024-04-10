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
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT
        = "NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL";
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP
        = "NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP";
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_1_LR
        = "NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL";
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR
        = "NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR";

    private static final String NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT_ACTIVITY_ID
        = "MediationSuccessfulNotifyApplicant";
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP_ACTIVITY_ID
        = "SendMediationSuccessfulDefendantLIP";
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_SOLICITOR_1_ACTIVITY_ID
        = "SendMediationSuccessfulDefendant1LR";
    private static final String NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_SOLICITOR_2_ACTIVITY_ID
        = "SendMediationSuccessfulDefendant2LR";

    public MediationSuccessfulTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSubmitSuccessfulMediationForLip() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, false,
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT_ACTIVITY_ID,
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

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP_ACTIVITY_ID,
                                   variables
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
    void shouldSubmitUnsuccessfulMediationFor1v1LR() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT_ACTIVITY_ID,
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

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_1_LR,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_SOLICITOR_1_ACTIVITY_ID,
                                   variables
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
    void shouldSubmitUnsuccessfulMediationFor1v2LR() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            TWO_RESPONDENT_REPRESENTATIVES, true,
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_CLAIMANT_ACTIVITY_ID,
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

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_1_LR,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_SOLICITOR_1_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR,
                                   NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_SOLICITOR_2_ACTIVITY_ID,
                                   variables
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
}
