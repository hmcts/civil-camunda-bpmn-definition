package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GeneralApplicationClosedTest extends GeneralAppBpmnBaseTest {

    //BPMN Settings
    private static final String CAMUNDA_FILE_NAME = "general_application_closed.bpmn";
    private static final String MESSAGE_NAME = "MAIN_CASE_CLOSED";
    private static final String PROCESS_ID = "MAIN_CASE_CLOSED";
    //Update Parent Claim
    private static final String UPDATE_PARENT_CLAIM_EVENT_ID = "APPLICATION_CLOSED_UPDATE_PARENT_CLAIM";
    private static final String UPDATE_PARENT_CLAIM_ACTIVITY_ID = "ApplicationClosedUpdateParentClaim";

    public GeneralApplicationClosedTest() {
        super(CAMUNDA_FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldUpdateParentClaimWithGeneralApplicationStatus_whenGeneralApplicationClosed() {
        VariableMap variables = Variables.createVariables();

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_GENERAL_APP_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_GENERAL_APP_BUSINESS_TOPIC,
                START_GENERAL_APP_BUSINESS_EVENT,
                START_GENERAL_APP_BUSINESS_ACTIVITY,
                variables
        );

        //update parent claim
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_GA_CASE_EVENT);
        assertCompleteExternalTask(
                applicantNotification,
                APPLICATION_EVENT,
                UPDATE_PARENT_CLAIM_EVENT_ID,
                UPDATE_PARENT_CLAIM_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_GENERAL_APP_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    /*
    @Test
    void shouldNotifyApplicantSolicitor_whenPastClaimDetailsNotificationDeadline() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                applicantNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
                "ClaimDismissedNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldNotifyAllParties_whenPastClaimDismissedDeadline(boolean has2RespondentSolicitors) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE");
        variables.put("flowFlags", Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, !has2RespondentSolicitors,
                TWO_RESPONDENT_REPRESENTATIVES, has2RespondentSolicitors));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                rpaNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
                "NotifyRoboticsOnCaseHandedOffline"
        );

        //complete the notification to respondent
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                respondentNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED",
                "ClaimDismissedNotifyRespondentSolicitor1"
        );

        if (has2RespondentSolicitors) {
            //complete the notification to respondent 2
            ExternalTask respondentTwoNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                    respondentTwoNotification,
                    PROCESS_CASE_EVENT,
                    "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED",
                    "ClaimDismissedNotifyRespondentSolicitor2"
            );
        }

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                applicantNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_DISMISSED",
                "ClaimDismissedNotifyApplicantSolicitor1"
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
     */
}
