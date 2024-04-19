package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HearingProcessTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "HEARING_SCHEDULED";
    public static final String PROCESS_ID = "HEARING_PROCESS";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_HEARING
        = "NOTIFY_CLAIMANT_HEARING";
    public static final String NOTIFY_DEFENDANT1_HEARING
        = "NOTIFY_DEFENDANT1_HEARING";
    public static final String NOTIFY_DEFENDANT2_HEARING
        = "NOTIFY_DEFENDANT2_HEARING";
    public static final String GENERATE_HEARING_FORM
        = "GENERATE_HEARING_FORM";
    public static final String CREATE_SERVICE_REQUEST_API
        = "CREATE_SERVICE_REQUEST_API";
    public static final String SEND_HEARING_TO_LIP_DEFENDANT
        = "SEND_HEARING_TO_LIP_DEFENDANT";
    public static final String SEND_HEARING_TO_LIP_CLAIMANT
        = "SEND_HEARING_TO_LIP_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT
        = "CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT
        = "CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT";

    //ACTIVITY IDs
    private static final String NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID
        = "NotifyClaimantHearing";
    private static final String NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID
        = "NotifyDefendant1Hearing";

    private static final String NOTIFY_DEFENDANT2_HEARING_ACTIVITY_ID
        = "NotifyDefendant2Hearing";
    public static final String GENERATE_HEARING_FORM_ACTIVITY_ID
        = "GenerateHearingForm";
    private static final String CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        = "ServiceRequestAPI";
    private static final String SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID
        = "SendHearingToDefendantLIP";
    private static final String SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID
        = "SendHearingToClaimantLIP";
    private static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID
        = "GenerateDashboardNotificationHearingScheduledClaimant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID
        = "GenerateDashboardNotificationHearingScheduledDefendant";

    public HearingProcessTest() {
        super("hearing_process.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyClaimantAndDefendantHearing_1v1() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID, variables
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID, variables
        );

        //complete the service request process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyClaimantAndDefendantHearing_1v2() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1 All Responses Received > Divergent Response
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, false,
            TWO_RESPONDENT_REPRESENTATIVES, true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY);

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID, variables
        );

        //complete the defendant2 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT2_HEARING, NOTIFY_DEFENDANT2_HEARING_ACTIVITY_ID, variables
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID, variables
        );

        //complete the service request process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyClaimantAndDefendantHearing_1v1_Lip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_HEARING_TO_LIP_DEFENDANT, SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_HEARING_TO_LIP_CLAIMANT, SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID, variables
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID, variables
        );

        //complete the service request process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyClaimantAndDefendantLipHearing_1v1() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            UNREPRESENTED_DEFENDANT_TWO, false,
            LIP_CASE, true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_HEARING_TO_LIP_DEFENDANT, SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_HEARING_TO_LIP_CLAIMANT, SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID, variables
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID
        );

        //complete the service request process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyClaimantAndDefendantLipHearing_1v2() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1 All Responses Received > Divergent Response
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            UNREPRESENTED_DEFENDANT_TWO, true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID, variables
        );

        //complete the defendant2 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT2_HEARING, NOTIFY_DEFENDANT2_HEARING_ACTIVITY_ID
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID
        );

        //complete the service request process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyDefendantLipHearing_1v1() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            UNREPRESENTED_DEFENDANT_TWO, false,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the hearing form generation
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_HEARING_TO_LIP_DEFENDANT, SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT1_HEARING_ACTIVITY_ID, variables
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID
        );

        //complete the service request process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        );

        //complete the dashboard notification process
       notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
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
