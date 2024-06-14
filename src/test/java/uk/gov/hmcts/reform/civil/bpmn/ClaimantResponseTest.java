package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClaimantResponseTest extends BpmnBaseTest {

    private static final String PROCEED_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String JUDICIAL_REFERRAL_EVENT = "JUDICIAL_REFERRAL";
    private static final String JUDICIAL_REFERRAL_ACTIVITY_ID = "JudicialReferral";

    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_UPDATE_GA_LOCATION";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "ClaimantResponseGenerateDirectionsQuestionnaire";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    public static final String PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID
        = "ProceedOfflineForResponseToDefence";

    public static final String PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID_MULTITRACK
        = "ProceedOfflineForResponseToDefenceMultitrack";
    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

    public ClaimantResponseTest() {
        super("claimant_response.bpmn", "CLAIMANT_RESPONSE_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponsewithDQAndProcessGALocationUpdate_WhenApplicantConfirmsToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            GENERAL_APPLICATION_ENABLED, true,
            "SDO_ENABLED", true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the Judicial Referral event
        ExternalTask judicialReferral = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            judicialReferral,
            PROCESS_CASE_EVENT,
            JUDICIAL_REFERRAL_EVENT,
            JUDICIAL_REFERRAL_ACTIVITY_ID,
            variables
        );

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
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
    void shouldSuccessfullyCompleteClaimantResponsewithDQAndProcessGALocationUpdateMultiClaim_WhenApplicantConfirmsToProceedMintiEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            GENERAL_APPLICATION_ENABLED, true,
            "SDO_ENABLED", true,
            IS_MULTI_TRACK, true,
            MINTI_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the Judicial Referral event
        ExternalTask judicialReferral = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            judicialReferral,
            PROCESS_CASE_EVENT,
            JUDICIAL_REFERRAL_EVENT,
            JUDICIAL_REFERRAL_ACTIVITY_ID,
            variables
        );

        //complete the Trigger and Update GA Location event
        ExternalTask triggerAndUpdateGenAppLocation = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            triggerAndUpdateGenAppLocation,
            PROCESS_CASE_EVENT,
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
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
            NOTIFY_RPA_ON_CONTINUOUS_FEED,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    //  claimant response with intention to proceed and claim is Multi track
    @Test
    void shouldSuccessfullyCompleteClaimantResponsewithDQAndMultiClaim_WhenApplicantConfirmsToProceedMintiNotEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            IS_MULTI_TRACK, true,
            MINTI_ENABLED, false
        ));

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
            PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID_MULTITRACK,
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
            "NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK",
            "ClaimantConfirmsToProceedNotifyRespondentSolicitor1Multitrack"
        );

        //complete the CC notification to applicant
        ExternalTask notifyApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyApplicant,
            PROCESS_CASE_EVENT,
            "NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK",
            "ClaimantConfirmsToProceedNotifyApplicantSolicitor1CCMultitrack"
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
        variables.put(FLOW_FLAGS, Map.of(
                GENERAL_APPLICATION_ENABLED, false,
                ONE_RESPONDENT_REPRESENTATIVE, false,
                TWO_RESPONDENT_REPRESENTATIVES, true
        ));

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

        //complete the notification to respondent
        ExternalTask notifyRespondent2 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent2,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED",
            "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor2"
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

    @Test
    void shouldSuccessfullyCompleteClaimantResponseWithQDAndProcessGA_WhenApplicantConfirmsNotToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_NOT_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
                GENERAL_APPLICATION_ENABLED, true,
                ONE_RESPONDENT_REPRESENTATIVE, false,
                TWO_RESPONDENT_REPRESENTATIVES, true
        ));

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

        //complete the notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED",
            "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor1"
        );

        //complete the notification to respondent
        ExternalTask notifyRespondent2 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent2,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED",
            "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor2"
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
