package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefendantResponseTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFENDANT_RESPONSE";
    public static final String PROCESS_ID = "DEFENDANT_RESPONSE_PROCESS_ID";

    public static final String OFFLINE_NOTIFY_APPLICANT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE";
    private static final String OFFLINE_NOTIFY_RESPONDENT_SOLICITOR_1
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE";
    private static final String OFFLINE_NOTIFICATION_RESPONDENT_ACTIVITY_ID
        = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1";
    private static final String OFFLINE_NOTIFICATION_APPLICANT_ACTIVITY_ID
        = "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1";

    public static final String FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE";
    public static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String FULL_DEFENCE_NOTIFICATION_ACTIVITY_ID
        = "DefendantResponseFullDefenceNotifyApplicantSolicitor1";
    private static final String FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "DefendantResponseFullDefenceGenerateDirectionsQuestionnaire";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    public DefendantResponseTest() {
        super("defendant_response.bpmn", "DEFENDANT_RESPONSE_PROCESS_ID");
    }

    @ParameterizedTest
    @ValueSource(strings = {"MAIN.RESPONDENT_FULL_ADMISSION", "MAIN.RESPONDENT_PART_ADMISSION", "MAIN.RESPONDENT_COUNTER_CLAIM"})
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

        //complete the notification to respondent
        ExternalTask forRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRespondent,
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

    @Test
    void shouldSuccessfullyCompleteOnline_whenRespondentFullDefenceResponse() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.RESPONDENT_FULL_DEFENCE");

        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to respondent
        ExternalTask forApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forApplicant,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_NOTIFY_APPLICANT_SOLICITOR_1,
            FULL_DEFENCE_NOTIFICATION_ACTIVITY_ID
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE,
            FULL_DEFENCE_GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
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

        VariableMap variables = Variables.createVariables();

        //fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
