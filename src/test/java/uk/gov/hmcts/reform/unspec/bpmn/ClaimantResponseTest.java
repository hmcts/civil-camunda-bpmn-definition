package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClaimantResponseTest extends BpmnBaseTest {

    private static final String PROCEED_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "ClaimantResponseGenerateDirectionsQuestionnaire";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    public static final String PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID
        = "ProceedOfflineForResponseToDefence";

    public ClaimantResponseTest() {
        super("claimant_response.bpmn", "CLAIMANT_RESPONSE_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponseWithQD_WhenApplicantConfirmsToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the take offline event
        ExternalTask takeOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            takeOffline,
            PROCESS_CASE_EVENT,
            PROCEED_OFFLINE_EVENT,
            PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_DIRECTIONS_QUESTIONNAIRE,
            GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID,
            variables
        );

        //complete the notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED",
            "ClaimantConfirmsToProceedNotifyRespondentSolicitor1"
        );

        //complete the CC notification to applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC",
            "ClaimantConfirmsToProceedNotifyApplicantSolicitor1CC"
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponseWithQD_WhenApplicantConfirmsNotToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_NOT_PROCEED");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the take offline event
        ExternalTask takeOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            takeOffline,
            PROCESS_CASE_EVENT,
            PROCEED_OFFLINE_EVENT,
            PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID,
            variables
        );

        //complete the notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED",
            "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor1"
        );

        //complete the CC notification to applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_CC",
            "ClaimantConfirmsNotToProceedNotifyApplicantSolicitor1CC"
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
