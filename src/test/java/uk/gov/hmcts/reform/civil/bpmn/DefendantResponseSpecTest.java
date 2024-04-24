package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefendantResponseSpecTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFENDANT_RESPONSE_SPEC";
    public static final String PROCESS_ID = "DEFENDANT_RESPONSE_PROCESS_ID_SPEC";

    //CCD EVENTS
    public static final String FULL_DEFENCE_RESPONSE_EVENT = "PROCESS_FULL_DEFENCE_SPEC";
    public static final String FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE";
    public static final String FULL_DEFENCE_NOTIFY_RESPONDENT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC";
    public static final String FULL_DEFENCE_NOTIFY_RESPONDENT_SOLICITOR_1_CC
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC";
    public static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String FULL_DEFENCE_GENERATE_SEALED_FORM = "GENERATE_RESPONSE_SEALED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    //ACTIVITY IDs
    private static final String FULL_DEFENCE_RESPONSE_ACTIVITY_ID = "FullDefenceResponse";
    private static final String FULL_DEFENCE_APPLICANT_NOTIFICATION_ACTIVITY_ID
        = "DefendantResponseFullDefenceNotifyApplicantSolicitor1";
    private static final String FULL_DEFENCE_RESPONDENT_NOTIFICATION_ACTIVITY_ID
        = "DefendantResponseFullDefenceNotifyRespondentSolicitor1CC";
    private static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "DefendantResponseFullDefenceGenerateDirectionsQuestionnaire";
    private static final String FULL_DEFENCE_GENERATE_SEALED_FORM_ACTIVITY_ID
        = "Activity_1ga6w9n";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";


    public DefendantResponseSpecTest() {
        super("defendant_response_spec.bpmn", "DEFENDANT_RESPONSE_PROCESS_ID_SPEC");
    }

    @Test
    void shouldSuccessfullyTriggerDashboardNotification_whenRespondentFullDefenceResponse() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            GENERAL_APPLICATION_ENABLED, false,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusinessTask = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusinessTask,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the full defence
        ExternalTask fullDefenceResponse = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            fullDefenceResponse,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_RESPONSE_EVENT,
            FULL_DEFENCE_RESPONSE_ACTIVITY_ID,
            variables
        );

        //complete the notification to applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1,
            FULL_DEFENCE_APPLICANT_NOTIFICATION_ACTIVITY_ID,
            variables
        );

        //complete the CC notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_RESPONDENT_SOLICITOR_1,
            FULL_DEFENCE_RESPONDENT_NOTIFICATION_ACTIVITY_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
            variables
        );

        //complete generate sealed form
        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_SEALED_FORM,
            FULL_DEFENCE_GENERATE_SEALED_FORM_ACTIVITY_ID,
            variables
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyTriggerDashboardNotification_whenRespondentNonFullDefenceResponse() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.COUNTER_CLAIM");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            GENERAL_APPLICATION_ENABLED, false,
            "COUNTER_CLAIM", true,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusinessTask = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusinessTask,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //proceed offline
        ExternalTask fullDefenceResponse = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            fullDefenceResponse,
            PROCESS_CASE_EVENT,
            "PROCEEDS_IN_HERITAGE_SYSTEM",
            "ProceedOfflineForNonDefenceResponse",
            variables
        );

        //complete the notification to respondent
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE",
            "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to applicant
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE",
            "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1",
            variables
        );

        //complete RPA
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
            "NotifyRoboticsOnCaseHandedOffline",
            variables
        );

        //complete generate sealed form
        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_SEALED_FORM,
            "Activity_1i4bh45",
            variables
        );

        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotTriggerDashboardNotification_whenAwaitingResponse() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, false,
            GENERAL_APPLICATION_ENABLED, true,
            TWO_RESPONDENT_REPRESENTATIVES, true,
            DASHBOARD_SERVICE_ENABLED, false
        ));

        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
            "Activity_0tyidsx",
            variables
        );

        //complete the notification to applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE",
            "Activity_0dne463",
            variables
        );

        //complete the CC notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_RESPONDENT_SOLICITOR_1_CC,
            "DefendantResponseFullDefenceNotifyRespondentSolicitor1",
            variables
        );

        //complete generate sealed form
        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_SEALED_FORM,
            "Activity_1fon6v1",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldNotTriggerDashboardNotification_whenDashboardNotEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            "FULL_DEFENCE", true,
            GENERAL_APPLICATION_ENABLED, false,
            DASHBOARD_SERVICE_ENABLED, false
        ));

        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the full defence
        ExternalTask fullDefenceResponse = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            fullDefenceResponse,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_RESPONSE_EVENT,
            FULL_DEFENCE_RESPONSE_ACTIVITY_ID,
            variables
        );

        //complete the notification to applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1,
            FULL_DEFENCE_APPLICANT_NOTIFICATION_ACTIVITY_ID,
            variables
        );

        //complete the CC notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_RESPONDENT_SOLICITOR_1,
            FULL_DEFENCE_RESPONDENT_NOTIFICATION_ACTIVITY_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
            variables
        );

        //complete generate sealed form
        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_SEALED_FORM,
            FULL_DEFENCE_GENERATE_SEALED_FORM_ACTIVITY_ID,
            variables
        );

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

    @Test
    void shouldNotTriggerDashboardNotification_whenRespondentNonFullDefenceResponseDashboardNotEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.COUNTER_CLAIM");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            GENERAL_APPLICATION_ENABLED, false,
            "COUNTER_CLAIM", true,
            DASHBOARD_SERVICE_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusinessTask = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusinessTask,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //proceed offline
        ExternalTask fullDefenceResponse = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            fullDefenceResponse,
            PROCESS_CASE_EVENT,
            "PROCEEDS_IN_HERITAGE_SYSTEM",
            "ProceedOfflineForNonDefenceResponse"
        );

        //complete the notification to respondent
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE",
            "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1"
        );

        //complete the notification to applicant
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE",
            "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1"
        );

        //complete RPA
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
            "NotifyRoboticsOnCaseHandedOffline"
        );

        //complete generate sealed form
        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_SEALED_FORM,
            "Activity_1i4bh45"
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

    private void createDefendantDashboardNotification() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_RESPONSE",
            "GenerateClaimantDashboardNotificationDefendantResponse"
        );
    }
}
