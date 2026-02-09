package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EnterBreathingSpaceSpecTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "ENTER_BREATHING_SPACE_SPEC";
    private static final String PROCESS_ID = "ENTER_BREATHING_SPACE_SPEC";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1 = "NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID = "BreathingSpaceEnterNotifyRespondentSolicitor1";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2 = "NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_2_ACTIVITY_ID = "BreathingSpaceEnterNotifyRespondentSolicitor2";
    private static final String NOTIFY_APPLICANT_SOLICITOR = "NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_APPLICANT_SOLICITOR_ACTIVITY_ID = "BreathingSpaceEnterNotifyApplicantSolicitor1";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsBreathingSpaceEnter";

    EnterBreathingSpaceSpecTest() {
        super("enter_breathing_space_spec.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyComplete_whenSingleRespondentRepresentative() {
        VariableMap variables = flowFlagVariables(true, false);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1,
            NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID,
            variables
        );

        assertApplicantAndRoboticsNotifications(variables);
    }

    @Test
    void shouldSuccessfullyComplete_whenTwoRespondentRepresentatives() {
        VariableMap variables = flowFlagVariables(false, true);

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        startBusinessProcess(variables);

        ExternalTask respondentOneNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentOneNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1,
            NOTIFY_RESPONDENT_SOLICITOR_1_ACTIVITY_ID,
            variables
        );

        ExternalTask respondentTwoNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentTwoNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_2,
            NOTIFY_RESPONDENT_SOLICITOR_2_ACTIVITY_ID,
            variables
        );

        assertApplicantAndRoboticsNotifications(variables);
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }

    private void assertApplicantAndRoboticsNotifications(VariableMap variables) {
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR,
            NOTIFY_APPLICANT_SOLICITOR_ACTIVITY_ID,
            variables
        );

        ExternalTask roboticsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            roboticsNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    private VariableMap flowFlagVariables(boolean oneRespondentRepresentative, boolean twoRespondentRepresentatives) {
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, oneRespondentRepresentative,
            TWO_RESPONDENT_REPRESENTATIVES, twoRespondentRepresentatives
        ));
        return variables;
    }
}
