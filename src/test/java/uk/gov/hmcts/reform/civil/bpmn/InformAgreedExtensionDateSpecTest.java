package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InformAgreedExtensionDateSpecTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "INFORM_AGREED_EXTENSION_DATE_SPEC";
    public static final String PROCESS_ID = "INFORM_AGREED_EXTENSION_DATE_SPEC_PROCESS_ID";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";

    private static final String CREATE_DASHBOARD_NOTIFICATION_MORE_TIME_REQUEST_FOR_RESPONDENT1 = "CREATE_DASHBOARD_NOTIFICATION_MORE_TIME_REQUEST_FOR_RESPONDENT1";
    private static final String CREATE_DASHBOARD_NOTIFICATION_MORE_TIME_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID
        = "GenerateDashboardNotificationMoreTimeRequestedForRespondent1";

    enum FlowState {
        PENDING_CLAIM_ISSUED;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public InformAgreedExtensionDateSpecTest() {
        super("inform_agreed_extension_date_spec.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "true, false",
        "false, true",
        "false, false"
    })
    void shouldSuccessfullyCompleteNotifyClaim_whenCalled(boolean lipClaimant, boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, lipClaimant,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives)
        );
        variables.putValue(FLOW_STATE,
                           FlowState.PENDING_CLAIM_ISSUED.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notifications to applicant
        if (lipClaimant) {
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                "NOTIFY_LIP_APPLICANT_FOR_AGREED_EXTENSION_DATE_FOR_SPEC",
                "AgreedExtensionDateNotifyApplicantLipForSpec",
                variables
            );

            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_DEFENDANT_RESPONSE_DATE",
                "GenerateClaimantDashboardNotificationClaimantNewResponseDeadlineLiPvLR",
                variables
            );
        } else {
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC",
                "AgreedExtensionDateNotifyApplicantSolicitor1ForSpec",
                variables
            );
        }

        //complete the CC notification to respondent solicitor 1
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC",
            "AgreedExtensionDateNotifyRespondentSolicitor1CCForSpec",
            variables
        );

        if (twoRepresentatives) {
            //complete the CC notification to respondent solicitor 2
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC",
                "AgreedExtensionDateNotifyRespondentSolicitor2CCForSpec",
                variables
            );
        }

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true, null"})
    void shouldSuccessfullyCompleteNotifyClaimDeadline_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(UNREPRESENTED_DEFENDANT_ONE, true,
                DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification of deadline to claimant
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION",
            "DefendantResponseDeadlineExtensionNotifyClaimant",
            variables
        );

        //complete the notification of deadline to defendant
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION",
            "DefendantResponseDeadlineExtensionNotifyDefendant",
            variables
        );

        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_DEFENDANT_RESPONSE_DATE",
            "GenerateClaimantDashboardNotificationClaimantNewResponseDeadline",
            variables
        );

        dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_MORE_TIME_REQUEST_FOR_RESPONDENT1,
            CREATE_DASHBOARD_NOTIFICATION_MORE_TIME_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID,
            variables
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
