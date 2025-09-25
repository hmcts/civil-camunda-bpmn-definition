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

class DefendantResponseTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFENDANT_RESPONSE";
    public static final String PROCESS_ID = "DEFENDANT_RESPONSE_PROCESS_ID";

    //CCD EVENTS
    public static final String NOTIFY_EVENT
        = "NOTIFY_EVENT";
    private static final String TAKE_CASE_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    public static final String FULL_DEFENCE_RESPONSE_EVENT = "PROCESS_FULL_DEFENCE";
    public static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    //ACTIVITY IDs
    private static final String OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
        = "DefendantResponseUnspecCaseHandedOfflineNotifyParties";
    private static final String TAKE_CASE_OFFLINE_ACTIVITY_ID = "ProceedOfflineForNonDefenceResponse";
    private static final String FULL_DEFENCE_RESPONSE_ACTIVITY_ID = "FullDefenceResponse";
    private static final String FULL_DEFENCE_ALL_PARTIES_NOTIFICATION_ACTIVITY_ID
        = "DefendantResponseUnspecFullDefenceNotifyParties";
    private static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "DefendantResponseFullDefenceGenerateDirectionsQuestionnaire";
    private static final String FIRST_RESPONSE_FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "FirstResponseFullDefenceGenerateDirectionsQuestionnaire";
    private static final String DIVERGENT_WITH_FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "DivergentDefendantResponseWithFullDefenceGenerateDirectionsQuestionnaire";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

    public DefendantResponseTest() {
        super("defendant_response.bpmn", "DEFENDANT_RESPONSE_PROCESS_ID");
    }

    @Nested
    class OneVsOneScenario {

        @ParameterizedTest
        @ValueSource(strings = {"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION", "MAIN.COUNTER_CLAIM"})
        void shouldSuccessfullyCompleteOfflineDefendantResponse_In1v1Scenario(String flowState) {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", flowState);
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, true
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

            //complete the proceedOffline event
            ExternalTask proceedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                proceedOffline,
                PROCESS_CASE_EVENT,
                TAKE_CASE_OFFLINE_EVENT,
                TAKE_CASE_OFFLINE_ACTIVITY_ID
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

            //complete the notification to all parties
            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }
    }

    @Nested
    class OneVsTwoScenario {

        @Test
        void shouldSuccessfullyGoOfflineDefendantResponse_In1v2_DivergentResponseScenario() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            //Setup Case as 1v2 All Responses Received > Divergent Response
            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, false,
                    TWO_RESPONDENT_REPRESENTATIVES, true
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

            //complete the proceedOffline event
            ExternalTask proceedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                proceedOffline,
                PROCESS_CASE_EVENT,
                TAKE_CASE_OFFLINE_EVENT,
                TAKE_CASE_OFFLINE_ACTIVITY_ID,
                variables
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

            //complete the notification to all parties
            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyGenerateDQAndGoOfflineDefendantResponse_In1v2_DivergentResponseWithFullDefence() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            //Setup Case as 1v2 All Responses Received > Divergent Response
            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, false,
                    TWO_RESPONDENT_REPRESENTATIVES, true
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

            //complete the document generation
            ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                documentGeneration,
                PROCESS_CASE_EVENT,
                FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
                DIVERGENT_WITH_FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
                variables
            );

            //complete the proceedOffline event
            ExternalTask proceedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                proceedOffline,
                PROCESS_CASE_EVENT,
                TAKE_CASE_OFFLINE_EVENT,
                TAKE_CASE_OFFLINE_ACTIVITY_ID,
                variables
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

            //complete the notification to all parties
            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @ParameterizedTest
        @ValueSource(strings = {"true", "false"})
        void shouldSuccessfullyStayOnline_In1v2Scenario(Boolean hasTwoRespondentRepresentatives) {

            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.FULL_DEFENCE");
            if (hasTwoRespondentRepresentatives) {
                variables.put(FLOW_FLAGS, Map.of(
                        ONE_RESPONDENT_REPRESENTATIVE, false,
                        TWO_RESPONDENT_REPRESENTATIVES, true
                ));
            } else {
                //Mock 1v1 Case (Do not email a second respondent)
                variables.put(FLOW_FLAGS, Map.of(
                        ONE_RESPONDENT_REPRESENTATIVE, true
                ));
            }

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

            //complete the notification to all parties
            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                FULL_DEFENCE_ALL_PARTIES_NOTIFICATION_ACTIVITY_ID,
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
    }

    @Nested
    class OneVsTwoAndFirstResponseReceived {

        @Test
        void shouldSuccessfullyCompleteTasks_whenOnlyOneResponseReceivedWithFullDefenceIn1v2Case() {
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
                    TWO_RESPONDENT_REPRESENTATIVES, true
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
                FIRST_RESPONSE_FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
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
        void shouldSuccessfullyCompleteProcess_whenOnlyOneResponseReceivedWithoutFullDefenceIn1v2Case() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            //complete the start business process
            ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);

            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED");
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, false,
                    TWO_RESPONDENT_REPRESENTATIVES, true
            ));

            assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
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
    }

    @Nested
    class GeneralApplicationEnabled {
        @ParameterizedTest
        @ValueSource(strings = {"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION", "MAIN.COUNTER_CLAIM"})
        void shouldSuccessfullyCompleteOfflineDefendantResponse_In1v1Scenario(String flowState) {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", flowState);
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, true
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

            //complete the proceedOffline event
            ExternalTask proceedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    proceedOffline,
                    PROCESS_CASE_EVENT,
                    TAKE_CASE_OFFLINE_EVENT,
                    TAKE_CASE_OFFLINE_ACTIVITY_ID
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

            //complete the notification to all parties
            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    forRobotics,
                    PROCESS_CASE_EVENT,
                    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyGoOfflineDefendantResponse_In1v2_DivergentResponseScenario() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            //Setup Case as 1v2 All Responses Received > Divergent Response
            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, false,
                    TWO_RESPONDENT_REPRESENTATIVES, true
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

            //complete the proceedOffline event
            ExternalTask proceedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    proceedOffline,
                    PROCESS_CASE_EVENT,
                    TAKE_CASE_OFFLINE_EVENT,
                    TAKE_CASE_OFFLINE_ACTIVITY_ID,
                    variables
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

            //complete the notification to all parties
            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    forRobotics,
                    PROCESS_CASE_EVENT,
                    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

        @Test
        void shouldSuccessfullyGenerateDQAndGoOfflineDefendantResponse_In1v2_DivergentResponseWithFullDefence() {
            //assert process has started
            assertFalse(processInstance.isEnded());

            //assert message start event
            assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

            //Setup Case as 1v2 All Responses Received > Divergent Response
            VariableMap variables = Variables.createVariables();
            variables.putValue("flowState", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
            variables.put(FLOW_FLAGS, Map.of(
                    ONE_RESPONDENT_REPRESENTATIVE, false,
                    TWO_RESPONDENT_REPRESENTATIVES, true
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

            //complete the document generation
            ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    documentGeneration,
                    PROCESS_CASE_EVENT,
                    FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
                    DIVERGENT_WITH_FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
                    variables
            );

            //complete the proceedOffline event
            ExternalTask proceedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    proceedOffline,
                    PROCESS_CASE_EVENT,
                    TAKE_CASE_OFFLINE_EVENT,
                    TAKE_CASE_OFFLINE_ACTIVITY_ID,
                    variables
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

            ExternalTask notifyAllParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyAllParties,
                    PROCESS_CASE_EVENT,
                    NOTIFY_EVENT,
                    OFFLINE_NOTIFICATION_TO_ALL_PARTIES_ACTIVITY_ID
            );

            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    forRobotics,
                    PROCESS_CASE_EVENT,
                    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
                    NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
            );

            //end business process
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);

            assertNoExternalTasksLeft();
        }

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
