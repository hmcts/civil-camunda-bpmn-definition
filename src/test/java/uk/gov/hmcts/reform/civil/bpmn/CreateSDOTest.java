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

class CreateSDOTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CREATE_SDO";
    public static final String PROCESS_ID = "CREATE_SDO";
    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_UPDATE_GA_LOCATION";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";
    public static final String GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO = "GenerateDashboardNotificationCreateSDO";

    public CreateSDOTest() {
        super("create_sdo.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTakeCaseOffline() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        //complete the Dashboard notifications for create SDO
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardTask,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
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
    void shouldSuccessfullyCompleteTakeCaseOfflineForLiPDefendant() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask partiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            partiesNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrderToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToClaimant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_CLAIMANT",
            "SendSDOToClaimantLIP",
            variables
        );

        ExternalTask sendSDOOrderToDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToDefendant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_DEFENDANT",
            "SendSDOToDefendantLIP",
            variables
        );

        //complete the Dashboard notifications for create SDO
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardTask,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
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
    void shouldSuccessfullyCompleteTakeCaseOfflineForLiPvLrClaim() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            LIP_CASE, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask partiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            partiesNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrderToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToClaimant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_CLAIMANT",
            "SendSDOToClaimantLIP",
            variables
        );

        //complete the Dashboard notifications for create SDO
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardTask,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
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
    void shouldSuccessfullyCompleteTakeCaseOfflineForLrvLrClaim() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            LIP_CASE, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to parties
        ExternalTask partiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            partiesNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "CreateSDONotify",
            variables
        );

        //complete the Dashboard notifications for create SDO
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardTask,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
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

    @ParameterizedTest
    @CsvSource({"true,false", "false,true", "true,true"})
    void shouldSkipTasksWhenLanguagePreferenceWelsh(boolean claimantBilingual, boolean defendantBilingual) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            WELSH_ENABLED, true,
            CLAIM_ISSUE_BILINGUAL, claimantBilingual,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, defendantBilingual
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the Dashboard notifications for create SDO
        ExternalTask dashboardTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardTask,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATION_CREATE_SDO,
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
