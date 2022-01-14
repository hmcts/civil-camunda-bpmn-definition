package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
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
    public static final String OFFLINE_NOTIFY_APPLICANT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE";
    private static final String OFFLINE_NOTIFY_RESPONDENT_SOLICITOR_1
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE";
    private static final String TAKE_CASE_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    public static final String FULL_DEFENCE_RESPONSE_EVENT = "PROCESS_FULL_DEFENCE";
    public static final String FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE";
    public static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    //ACTIVITY IDs
    private static final String OFFLINE_NOTIFICATION_APPLICANT_ACTIVITY_ID
        = "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1";
    private static final String OFFLINE_NOTIFICATION_RESPONDENT_ACTIVITY_ID
        = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1";
    private static final String TAKE_CASE_OFFLINE_ACTIVITY_ID = "ProceedOfflineForNonDefenceResponse";
    private static final String FULL_DEFENCE_RESPONSE_ACTIVITY_ID = "FullDefenceResponse";
    private static final String FULL_DEFENCE_NOTIFICATION_ACTIVITY_ID
        = "DefendantResponseFullDefenceNotifyApplicantSolicitor1";
    //to be removed when new DQ is created for respondent solicitor 2
    public static final String ONE_RESPONSE_RECEIVED_TEMP_EMAIL_ACTIVITY_ID
        = "DefendantResponseOneResponseReceivedNotifyApplicantSolicitor1";
    private static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "DefendantResponseFullDefenceGenerateDirectionsQuestionnaire";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";

    public DefendantResponseTest() {
        super("defendant_response.bpmn", "DEFENDANT_RESPONSE_PROCESS_ID");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION", "MAIN.COUNTER_CLAIM",
        "MAIN.DIVERGENT_RESPOND"})
    void shouldSuccessfullyCompleteOfflineDefendantResponse(String flowState) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusinessTask = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", flowState);
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

        //complete the notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            OFFLINE_NOTIFY_RESPONDENT_SOLICITOR_1,
            OFFLINE_NOTIFICATION_RESPONDENT_ACTIVITY_ID
        );

        //complete the notification to applicant
        ExternalTask forApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forApplicant,
            PROCESS_CASE_EVENT,
            OFFLINE_NOTIFY_APPLICANT_SOLICITOR_1,
            OFFLINE_NOTIFICATION_APPLICANT_ACTIVITY_ID
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
    void shouldSuccessfullyCompleteOnline_whenRespondentFullDefenceResponse(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE");
        variables.put(FLOW_FLAGS, Map.of(RPA_CONTINUOUS_FEED, rpaContinuousFeed));

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
            FULL_DEFENCE_NOTIFICATION_ACTIVITY_ID,
            variables
        );

        //complete the CC notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC",
            "DefendantResponseFullDefenceNotifyRespondentSolicitor1CC",
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

        if (rpaContinuousFeed) {
            //complete the Robotics notification
            ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                forRobotics,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteOneTask_whenOnlyOneResponseReceivedIn1v2Case() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.AWAITING_RESPONSES_RECEIVED");

        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        // TODO: this will be refactored to be a DQ rather than an applicant notification. Covered in CMC-1653
        // notifyApplicant is a placeholder until we can generate a DQ for respondent 2 solicitor
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1,
            ONE_RESPONSE_RECEIVED_TEMP_EMAIL_ACTIVITY_ID,
            variables
        );

        //TODO: Remove the above and uncomment this when CMC-1653 is completed
        //complete the DQ document generation
        /*
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
            ONE_RESPONSE_RECEIVED_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
            variables
        );
         */

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
