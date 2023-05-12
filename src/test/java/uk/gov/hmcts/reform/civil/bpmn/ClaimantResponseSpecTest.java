package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClaimantResponseSpecTest extends BpmnBaseTest {

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
    private static final String NOTIFY_LIP_RESP_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED =
        "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_LIP";
    private static final String NOTIFY_LIP_RESP_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_ACTIVITY_ID =
        "ClaimantConfirmsNotToProceedNotifyRespondentSolicitor1Lip";
    public static final String PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID
        = "ProceedOfflineForResponseToDefence";
    private static final String LR_V_LIP_ENABLED = "LR_V_LIP_ENABLED";
    private static final String NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED =
        "NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED";
    private static final String NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED_ACTIVITY_ID =
        "ClaimantAgreedSettledPartAdmitNotifyLip";

    public ClaimantResponseSpecTest() {
        super("claimant_response_spec.bpmn", "CLAIMANT_RESPONSE_PROCESS_ID_SPEC");
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponseWithQD_WhenApplicantConfirmsToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT1_FOR_CLAIMANT_AGREED_REPAYMENT",
            "ClaimantAgreedRepaymentNotifyRespondent1"
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
    void shouldSuccessfullyCompleteClaimantResponseWithQD_WhenApplicantRejectToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notifyClimantLR = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyClimantLR,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT",
            "ClaimantDisAgreeRepaymentPlanNotifyApplicant"
        );

        ExternalTask notifyLipDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyLipDefendant,
            PROCESS_CASE_EVENT,
            "NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT",
            "ClaimantDisAgreedRepaymentPlanNotifyLip"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponseAndProcessGenAppLocationWithDQ_WhenApplicantConfirmsToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            AGREED_TO_MEDIATION, false,
            GENERAL_APPLICATION_ENABLED, true,
            SDO_ENABLED, true
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
    void shouldSuccessfullyCompleteClaimantResponseToLip_WhenApplicantConfirmsNotToProceed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_NOT_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true
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

        //complete the notification to LIP respondent
        ExternalTask notifyLipRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyLipRespondent,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESP_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED,
            NOTIFY_LIP_RESP_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponse_WhenInMediation() {

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.IN_MEDIATION");
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponse_WhenApplicantToSettle() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_AGREE_SETTLE");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification to LIP respondent
        ExternalTask notifyLipRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyLipRespondent,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED,
            NOTIFY_LIP_DEFENDANT_PART_ADMIT_CLAIM_SETTLED_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponse_LrVLipFlagIsOn() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            LR_V_LIP_ENABLED, true
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

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
