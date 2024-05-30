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

class GenerateOrderNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "GENERATE_ORDER_NOTIFICATION";
    public static final String PROCESS_ID = "GENERATE_ORDER_NOTIFICATION";

    //CCD CASE EVENT
    public static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER";
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER";
    public static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER";
    public static final String SEND_FINAL_ORDER_TO_LIP_DEFENDANT
        = "SEND_FINAL_ORDER_TO_LIP_DEFENDANT";
    public static final String SEND_FINAL_ORDER_TO_LIP_CLAIMANT
        = "SEND_FINAL_ORDER_TO_LIP_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT
        = "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT
        = "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyRespondentSolicitor2";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyRespondentSolicitor1";
    public static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyApplicantSolicitor1";
    private static final String SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID
        = "SendFinalOrderToDefendantLIP";
    private static final String SEND_FINAL_ORDER_TO_LIP_CLAIMANT_ACTIVITY_ID
        = "SendFinalOrderToClaimantLIP";
    private static final String CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT_ACTIVITY_ID
        = "GenerateDashboardNotificationFinalOrderClaimant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT_ACTIVITY_ID
        = "GenerateDashboardNotificationFinalOrderDefendant";

    public GenerateOrderNotificationTest() {
        super("generate_order_notification.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteGenerateOrderNotifications(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        if (twoRepresentatives) {
            //complete the defendant2 notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                       variables
            );
        }

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );
        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT_ACTIVITY_ID,
                                   variables
        );
        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsLip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            DASHBOARD_SERVICE_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsLipAndGenerateDashboard() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT_ACTIVITY_ID,
                                   variables
        );
        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsAndBulkPrintLip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, true,
            DASHBOARD_SERVICE_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_DEFENDANT, SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_CLAIMANT, SEND_FINAL_ORDER_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsAndBulkPrintLipAndDashboard() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, true,
            DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_DEFENDANT, SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_CLAIMANT, SEND_FINAL_ORDER_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT_ACTIVITY_ID,
                                   variables
        );
        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsAndBulkPrintDefendantLip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, false,
            DASHBOARD_SERVICE_ENABLED, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_DEFENDANT, SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsAndBulkPrintDefendantLipWhenDashboardNotPresent() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_DEFENDANT, SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
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
