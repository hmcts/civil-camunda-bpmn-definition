package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotifyDecisionOnReconsiderationRequestTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DECISION_ON_RECONSIDERATION_REQUEST";
    public static final String PROCESS_ID = "DECISION_ON_RECONSIDERATION_REQUEST";

    public NotifyDecisionOnReconsiderationRequestTest() {
        super("notify_decision_on_reconsideration_request.bpmn", "DECISION_ON_RECONSIDERATION_REQUEST");
    }

    @Test
    void shouldSuccessfullyNotifyDecisionOnReconsiderationRequestWithDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //complete the dashboard notification for Claimant
        ExternalTask claimantDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_CLAIMANT1",
            "GenerateDashboardNotificationDecisionReconsiderationClaimant",
            variables
        );

        //complete the dashboard notification for Respondent
        ExternalTask respondentDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1",
            "GenerateDashboardNotificationDecisionReconsiderationDefendant",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyNotifyDecisionOnReconsiderationRequestWithoutDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyTriggerBulkPrintForOnlyClaimant() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, false,
                                          LIP_CASE, true,
                                          WELSH_ENABLED, true,
                                          UNREPRESENTED_DEFENDANT_ONE, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete trigger Bulk Print for Claimant
        ExternalTask sendBulkPrintForClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendBulkPrintForClaimant,
            PROCESS_CASE_EVENT,
            "SEND_DRO_ORDER_TO_LIP_CLAIMANT",
            "SendDORToClaimantLIP",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotTriggerBulkPrintForClaimantWhenWelshFlagDisabledForLipCase() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, false,
                                          LIP_CASE, true,
                                          WELSH_ENABLED, false,
                                          UNREPRESENTED_DEFENDANT_ONE, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyTriggerBulkPrintForOnlyDefendantAndDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, true,
                                          LIP_CASE, false,
                                          WELSH_ENABLED, true,
                                          UNREPRESENTED_DEFENDANT_ONE, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete trigger Bulk Print for Claimant
        ExternalTask sendBulkPrintForDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendBulkPrintForDefendant,
            PROCESS_CASE_EVENT,
            "SEND_DRO_ORDER_TO_LIP_DEFENDANT",
            "SendToDefendantLIP",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //complete the dashboard notification for Claimant
        ExternalTask claimantDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_CLAIMANT1",
            "GenerateDashboardNotificationDecisionReconsiderationClaimant",
            variables
        );

        //complete the dashboard notification for Respondent
        ExternalTask respondentDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1",
            "GenerateDashboardNotificationDecisionReconsiderationDefendant",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyTriggerBulkPrintForOnlyDefendantWithoutDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(UNREPRESENTED_DEFENDANT_ONE, true,
                                          WELSH_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete trigger Bulk Print for Claimant
        ExternalTask sendBulkPrintForDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendBulkPrintForDefendant,
            PROCESS_CASE_EVENT,
            "SEND_DRO_ORDER_TO_LIP_DEFENDANT",
            "SendToDefendantLIP",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotTriggerBulkPrintForDefendantWhenLipDefendantAndWelshFlagDisabledWithoutDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(UNREPRESENTED_DEFENDANT_ONE, true,
                                          WELSH_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyTriggerBulkPrintForBothClaimantNDefendantAndDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, true,
                                          LIP_CASE, true,
                                          WELSH_ENABLED, true,
                                          UNREPRESENTED_DEFENDANT_ONE, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete trigger Bulk Print for Claimant
        ExternalTask sendBulkPrintForClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendBulkPrintForClaimant,
            PROCESS_CASE_EVENT,
            "SEND_DRO_ORDER_TO_LIP_CLAIMANT",
            "SendDORToClaimantLIP",
            variables
        );

        //complete trigger Bulk Print for Defendant
        ExternalTask sendBulkPrintForDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendBulkPrintForDefendant,
            PROCESS_CASE_EVENT,
            "SEND_DRO_ORDER_TO_LIP_DEFENDANT",
            "SendToDefendantLIP",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //complete the dashboard notification for Claimant
        ExternalTask claimantDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_CLAIMANT1",
            "GenerateDashboardNotificationDecisionReconsiderationClaimant",
            variables
        );

        //complete the dashboard notification for Respondent
        ExternalTask respondentDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1",
            "GenerateDashboardNotificationDecisionReconsiderationDefendant",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyTriggerBulkPrintForOnlyClaimantAndDashboardEvents() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, true,
                                          LIP_CASE, true,
                                          WELSH_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete trigger Bulk Print for Claimant
        ExternalTask sendBulkPrintForDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendBulkPrintForDefendant,
            PROCESS_CASE_EVENT,
            "SEND_DRO_ORDER_TO_LIP_CLAIMANT",
            "SendDORToClaimantLIP",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
            variables
        );

        //complete the dashboard notification for Claimant
        ExternalTask claimantDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_CLAIMANT1",
            "GenerateDashboardNotificationDecisionReconsiderationClaimant",
            variables
        );

        //complete the dashboard notification for Respondent
        ExternalTask respondentDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentDashboardNotification,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_DECISION_RECONSIDERATION_DEFENDANT1",
            "GenerateDashboardNotificationDecisionReconsiderationDefendant",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyNotTriggerBulkPrintWhenItIsNotLipCase() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(CASE_PROGRESSION_ENABLED, false,
                                          LIP_CASE, false,
                                          WELSH_ENABLED, true,
                                          UNREPRESENTED_DEFENDANT_ONE, false
                                          ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT",
            "Activity_0nyrqab",
            variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk",
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
