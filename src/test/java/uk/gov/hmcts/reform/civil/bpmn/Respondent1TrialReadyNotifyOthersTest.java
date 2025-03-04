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

class Respondent1TrialReadyNotifyOthersTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS";
    public static final String PROCESS_ID = "RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS";

    //CCD CASE EVENT
    public static final String RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY
        = "RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY";
    public static final String GENERATE_TRIAL_READY_FORM_RESPONDENT1
        = "GENERATE_TRIAL_READY_FORM_RESPONDENT1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT
        = "CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT";
    //ACTIVITY IDs
    public static final String RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY_ACTIVITY_ID
        = "RespondentSolicitor1TrialReadyNotifierOthers";
    public static final String GENERATE_TRIAL_READY_FORM_RESPONDENT1_ACTIVITY_ID
        = "GenerateTrialReadyFormRespondent1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT_ACTIVITY_ID
        = "GenerateClaimantDashboardNotificationTrialArrangementsNotifyParty";

    public Respondent1TrialReadyNotifyOthersTest() {
        super("respondent1_trial_ready_notify_others.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyFormAndNotifyClaimantAndDefendantHearing() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true,
            CASE_PROGRESSION_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the notifications
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY,
                                   RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY_ACTIVITY_ID,
                                   variables
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT1,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT1_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyFormAndNotifyClaimantAndDefendantHearingLiP() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, true,
            CASE_PROGRESSION_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the notifications
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY,
                                   RESPONDENT1_NOTIFY_OTHERS_TRIAL_READY_ACTIVITY_ID,
                                   variables
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT1,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT1_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_CLAIMANT_ACTIVITY_ID,
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
