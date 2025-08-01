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

class DefendantResponseSpecTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFENDANT_RESPONSE_SPEC";
    public static final String PROCESS_ID = "DEFENDANT_RESPONSE_PROCESS_ID_SPEC";

    //CCD EVENTS
    public static final String FULL_DEFENCE_RESPONSE_EVENT = "PROCESS_FULL_DEFENCE_SPEC";
    public static final String NOTIFY_EVENT
        = "NOTIFY_EVENT";
    public static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String FULL_DEFENCE_GENERATE_SEALED_FORM = "GENERATE_RESPONSE_SEALED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    //ACTIVITY IDs
    private static final String FULL_DEFENCE_RESPONSE_ACTIVITY_ID = "FullDefenceResponse";
    private static final String ONE_RESP_RESPOND_ACTIVITY_ID
        = "DefendantResponseSpecOneRespRespondedNotifyParties";
    private static final String LR_FULL_DEFENCE_FULL_ADMIT_PART_ADMIT_ACTIVITY_ID
        = "DefendantResponseSpecFullDefenceFullPartAdmitNotifyParties";
    private static final String LIP_V_LR_FULL_ADMIT_PART_ADMIT_ACTIVITY_ID
        = "DefendantResponseSpecLipvLRFullOrPartAdmit";
    private static final String COUNTER_CLAIM_DIVERGED_RESP_ACTIVITY_ID
        = "DefendantResponseSpecCaseHandedOfflineNotifyParties";
    private static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "DefendantResponseFullDefenceGenerateDirectionsQuestionnaire";
    private static final String FULL_DEFENCE_GENERATE_SEALED_FORM_ACTIVITY_ID
        = "Activity_1ga6w9n";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

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

        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            LR_FULL_DEFENCE_FULL_ADMIT_PART_ADMIT_ACTIVITY_ID,
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
            NOTIFY_EVENT,
            COUNTER_CLAIM_DIVERGED_RESP_ACTIVITY_ID,
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

        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            ONE_RESP_RESPOND_ACTIVITY_ID,
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

        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            LR_FULL_DEFENCE_FULL_ADMIT_PART_ADMIT_ACTIVITY_ID,
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

        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            COUNTER_CLAIM_DIVERGED_RESP_ACTIVITY_ID
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

    @ParameterizedTest
    @CsvSource({"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION"})
    void shouldMoveCaseOfflineForLiPvLrClaim(String responseType) {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", responseType);
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
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

        if (responseType.equals("MAIN.PART_ADMISSION")) {
            //generate DQ
            ExternalTask generateDQ = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                generateDQ,
                PROCESS_CASE_EVENT,
                "GENERATE_DIRECTIONS_QUESTIONNAIRE",
                "DefendantResponsePartAdmitGenerateDirectionsQuestionnaire"
            );
        }

        //proceed offline
        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            "GENERATE_RESPONSE_SEALED",
            "DefendantResponseFullOrPartAdmitGenerateSealedForm"
        );

        //proceed offline
        ExternalTask fullDefenceResponse = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            fullDefenceResponse,
            PROCESS_CASE_EVENT,
            "PROCEEDS_IN_HERITAGE_SYSTEM",
            "ProceedOffline"
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
            "Activity_0ncmkab"
        );

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateApplicationStatus,
            PROCESS_CASE_EVENT,
            TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
            APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimWithApplicationStatus,
            PROCESS_CASE_EVENT,
            APPLICATION_OFFLINE_UPDATE_CLAIM,
            APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        //complete the notification to LIP applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            LIP_V_LR_FULL_ADMIT_PART_ADMIT_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldPauseTheNotificationsForWelshClaimantDuringTranslation_FullAdmit() {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMISSION");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            WELSH_ENABLED, true,
            CLAIM_ISSUE_BILINGUAL, true
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

        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            "GENERATE_RESPONSE_SEALED",
            "Activity_0nakdad"
        );

        ExternalTask removeDJNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            removeDJNotificationTask,
            PROCESS_CASE_EVENT,
            "REMOVE_CLAIMANT_DJ_DASHBOARD_NOTIFICATION",
            "Activity_1phjbuy"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldPauseTheNotificationsForWelshClaimantDuringTranslation_PartAdmit() {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMISSION");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            WELSH_ENABLED, true,
            CLAIM_ISSUE_BILINGUAL, true
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

        ExternalTask generateDQ = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDQ,
            PROCESS_CASE_EVENT,
            "GENERATE_DIRECTIONS_QUESTIONNAIRE",
            "Activity_1dkbh3e"
        );

        ExternalTask generateSealedForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateSealedForm,
            PROCESS_CASE_EVENT,
            "GENERATE_RESPONSE_SEALED",
            "Activity_0nakdad"
        );

        ExternalTask removeDJNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            removeDJNotificationTask,
            PROCESS_CASE_EVENT,
            "REMOVE_CLAIMANT_DJ_DASHBOARD_NOTIFICATION",
            "Activity_1phjbuy"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
