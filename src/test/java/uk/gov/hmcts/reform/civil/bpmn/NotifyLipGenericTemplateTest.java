package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

class NotifyLipGenericTemplateTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "CITIZEN_HEARING_FEE_PAYMENT";
    private static final String PROCESS_ID = "NOTIFY_LIP_GENERIC_TEMPLATE";

    private static final String NOTIFY_APPLICANT1_GENERIC_TEMPLATE = "NOTIFY_EVENT";
    private static final String NOTIFY_APPLICANT1_GENERIC_TEMPLATE_ACTIVITY_ID = "NotifyLipGenericTemplateNotifier";

    private static final String GENERATE_DASHBOARD_NOTIFICATIONS_ACTIVITY_ID =
        "GenerateDashboardNotificationsCitizenHearingFeePayment";

    NotifyLipGenericTemplateTest() {
        super("notify_lip_generic_template.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteNotifyLipGenericTemplate_whenCalled() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);

        VariableMap variables = Variables.createVariables();

        startBusinessProcess(variables);

        ExternalTask notifyApplicant1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant1,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT1_GENERIC_TEMPLATE,
            NOTIFY_APPLICANT1_GENERIC_TEMPLATE_ACTIVITY_ID
        );

        ExternalTask generateDashboardNotifications = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDashboardNotifications,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            GENERATE_DASHBOARD_NOTIFICATIONS_ACTIVITY_ID
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
