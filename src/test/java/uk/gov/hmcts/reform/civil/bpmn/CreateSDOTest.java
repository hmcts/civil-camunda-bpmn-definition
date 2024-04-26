package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSDOTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CREATE_SDO";
    public static final String PROCESS_ID = "CREATE_SDO";
    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_UPDATE_GA_LOCATION";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";

    public CreateSDOTest() {
        super("create_sdo.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTakeCaseOfflineWhenGeneralApplicationDisabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, false,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //complete the notification dashboard
        ExternalTask dashboardClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardClaimant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT",
            "GenerateDashboardNotificationSDOClaimant",
            variables
        );

        //complete the notification dashboard
        ExternalTask dashboardDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardDefendant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT",
            "GenerateDashboardNotificationSDODefendant",
            variables
        );

        //complete the notification to respondent 1 dashboard
        ExternalTask respondent1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1DashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT",
            "Activity_Notice_Hearing_Claimant",
            variables
        );
        //complete the notification to defendant 1 dashboard
        ExternalTask applicant1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicant1DashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_DEFENDANT",
            "Activity_Notice_Hearing_Defendant",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteTakeCaseOfflineWhenGeneralApplicationDisabledDashboardDisabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteTakeCaseOfflineWhenGeneralApplicationEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, true,
            DASHBOARD_SERVICE_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteTakeCaseOfflineWhenGeneralApplicationEnabledForLiPDefendant() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, true,
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, true,
            LIP_CASE, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrder = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrder,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_DEFENDANT",
            "SendSDOToDefendantLIP",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //complete the notification dashboard
        ExternalTask dashboardClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardClaimant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT",
            "GenerateDashboardNotificationSDOClaimant",
            variables
        );

        //complete the notification dashboard
        ExternalTask dashboardDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardDefendant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT",
            "GenerateDashboardNotificationSDODefendant",
            variables
        );

        //complete the notification to respondent 1 dashboard
        ExternalTask respondent1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1DashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT",
            "Activity_Notice_Hearing_Claimant",
            variables
        );
        //complete the notification to defendant 1 dashboard
        ExternalTask applicant1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicant1DashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_DEFENDANT",
            "Activity_Notice_Hearing_Defendant",
            variables
        );

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyPassDashboardTasksWhenDashboardServiceEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            GENERAL_APPLICATION_ENABLED, false,
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrder = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrder,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_DEFENDANT",
            "SendSDOToDefendantLIP",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //complete the notification dashboard
        ExternalTask dashboardClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardClaimant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT",
            "GenerateDashboardNotificationSDOClaimant",
            variables
        );

        //complete the notification dashboard
        ExternalTask dashboardDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardDefendant,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT",
            "GenerateDashboardNotificationSDODefendant",
            variables
        );

        //complete the notification to respondent 1 dashboard
        ExternalTask respondent1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1DashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_CLAIMANT",
            "Activity_Notice_Hearing_Claimant",
            variables
        );
        //complete the notification to defendant 1 dashboard
        ExternalTask applicant1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicant1DashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_UPLOAD_HEARING_DOCUMENTS_DEFENDANT",
            "Activity_Notice_Hearing_Defendant",
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
