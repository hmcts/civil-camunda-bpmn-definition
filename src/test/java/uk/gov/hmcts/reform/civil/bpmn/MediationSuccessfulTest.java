package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class MediationSuccessfulTest extends BpmnBaseTest {

    private static final String FILE_NAME = "mediation_successful.bpmn";
    private static final String MESSAGE_NAME = "MEDIATION_SUCCESSFUL";
    private static final String PROCESS_ID = "MEDIATION_SUCCESSFUL_ID";

    public MediationSuccessfulTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSubmitSuccessfulMediation() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);

        ExternalTask notifyApplicantLR = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicantLR,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL",
            "MediationSuccessfulNotifyApplicant"
        );

        //dashboarNotification applicant
        ExternalTask dashboardApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardApplicant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_FOR_MEDIATION_SUCCESSFUL_FOR_APPLICANT",
            "GenerateDashboardNotificationClaimantMediationSuccessful"
        );

        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL",
            "MediationSuccessfulNotifyRespondent"
        );

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
