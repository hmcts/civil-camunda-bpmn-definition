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

class ClaimantResponseSpecTest extends BpmnBaseTest {

    private static final String PROCEED_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String PROCEED_OFFLINE_EVENT_ACTIVITY_ID = "proceedsInHeritageSystem";
    private static final String JUDICIAL_REFERRAL_EVENT = "JUDICIAL_REFERRAL";
    private static final String JUDICIAL_REFERRAL_ACTIVITY_ID = "JudicialReferral";

    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_UPDATE_GA_LOCATION";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE = "GENERATE_DIRECTIONS_QUESTIONNAIRE";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE_ACTIVITY_ID
        = "ClaimantResponseGenerateDirectionsQuestionnaire";
    private static final String GENERATE_DIRECTIONS_QUESTIONNAIRE_FOR_TRANSLATION_ACTIVITY_ID
        = "ClaimantResponseGenerateDirectionsQuestionnaireForTranslation";
    private static final String GENERATE_CLAIMANT_DQ_MEDITATION_ACTIVITY_ID = "ClaimantGenerateDQMeditation";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    private static final String NOTIFY_LIP_RESP_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_ACTIVITY_ID =
        "ClaimantResponseConfirmsNotToProceedLipNotify";
    public static final String PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID
        = "ProceedOfflineForResponseToDefence";
    private static final String CLAIMANT_RESPONSE_AGREED_SETTLED_PART_ADMIT_NOTIFY_ACTIVITY_ID =
        "ClaimantResponseAgreedSettledPartAdmitNotify";
    private static final String CLAIMANT_RESPONSE_NOT_AGREED_REPAYMENT_ACTIVITY_ID = "ClaimantResponseNotAgreedRepaymentNotify";
    private static final String CLAIMANT_RESPONSE_AGREED_REPAYMENT_ACTIVITY_ID = "ClaimantResponseAgreedRepaymentNotify";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE = "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID = "GenerateDashboardNotificationRespondent1";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_CONFIRMS_NOT_TO_PROCEED_EVENT_ID = "ClaimantResponseConfirmsNotToProceedNotify";

    private static final String NOTIFY_EVENT = "NOTIFY_EVENT";

    private static final String NOTIFY_RESPONDENT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED = "NOTIFY_RESPONDENT1_PART_ADMIT_PAY_IMMEDIATELY_AGREED";
    private static final String NOTIFY_APPLICANT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED = "NOTIFY_APPLICANT_PART_ADMIT_PAY_IMMEDIATELY_AGREED";
    private static final String NOTIFY_RESPONDENT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED_ACTIVITY_ID = "DefendantPartAdmitPayImmediatelyNotification";
    private static final String NOTIFY_APPLICANT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED_ACTIVITY_ID = "ClaimantPartAdmitPayImmediatelyNotification";

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
        variables.putValue("flowFlags", Map.of(DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the Robotics notification
        ExternalTask proccedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proccedOffline,
            PROCESS_CASE_EVENT,
            PROCEED_OFFLINE_EVENT,
            "Activity_00lleen",
            variables
        );

        //complete the notification to respondent
        ExternalTask notifyRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRespondent,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            CLAIMANT_RESPONSE_AGREED_REPAYMENT_ACTIVITY_ID
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
        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteWhenPayImmediately() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_PAY_IMMEDIATELY");
        variables.putValue("flowFlags", Map.of(
            DASHBOARD_SERVICE_ENABLED, true,
            JO_ONLINE_LIVE_ENABLED, false
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

        //complete the Robotics notification
        ExternalTask proccedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proccedOffline,
            PROCESS_CASE_EVENT,
            PROCEED_OFFLINE_EVENT,
            PROCEED_OFFLINE_EVENT_ACTIVITY_ID,
            variables
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
        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteWhenPayImmediately_JoFlagEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_PAY_IMMEDIATELY");
        variables.putValue("flowFlags", Map.of(
            DASHBOARD_SERVICE_ENABLED, true,
            JO_ONLINE_LIVE_ENABLED, true
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

        createDefendantDashboardNotification();

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
        variables.putValue("flowFlags", Map.of(DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            "ClaimantResponseNotAgreedRepaymentNotify"
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
        createDefendantDashboardNotification();

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
            "SDO_ENABLED", true,
            DASHBOARD_SERVICE_ENABLED, true
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

        //complete the notification to all parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            "ClaimantConfirmsToProceedNotify"
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

        ExternalTask defendantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT",
            "defendantLipApplicationOfflineDashboardNotification"
        );

        createDefendantDashboardNotification();

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
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, true
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

        //complete the notification to LIP respondent
        ExternalTask notifyLipRespondent = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyLipRespondent,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            NOTIFY_LIP_RESP_FOR_CLAIMANT_CONFIRMS_NOT_TO_PROCEED_ACTIVITY_ID
        );
        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponseToLip_WhenApplicantConfirmsNotToProceed_PreCUIR2() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_NOT_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, false
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
            NOTIFY_EVENT,
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
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            DASHBOARD_SERVICE_ENABLED, true,
            GENERAL_APPLICATION_ENABLED, false));

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            "ClaimantDefendantAgreedMediationNotify"
        );

        ExternalTask generateDQ = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDQ,
            PROCESS_CASE_EVENT,
            GENERATE_DIRECTIONS_QUESTIONNAIRE,
            GENERATE_CLAIMANT_DQ_MEDITATION_ACTIVITY_ID
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
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponse_WhenInMediation1v2DifferentSol() {

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.IN_MEDIATION");
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, false,
            TWO_RESPONDENT_REPRESENTATIVES, true,
            DASHBOARD_SERVICE_ENABLED, true));

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            "ClaimantDefendantAgreedMediationNotify"
        );

        ExternalTask generateDQ = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDQ,
            PROCESS_CASE_EVENT,
            GENERATE_DIRECTIONS_QUESTIONNAIRE,
            GENERATE_CLAIMANT_DQ_MEDITATION_ACTIVITY_ID
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
        variables.putValue("flowFlags", Map.of(DASHBOARD_SERVICE_ENABLED, true));

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
            NOTIFY_EVENT,
            CLAIMANT_RESPONSE_AGREED_SETTLED_PART_ADMIT_NOTIFY_ACTIVITY_ID
        );
        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldCompletePartAdmitPayImmediately_WhenApplicantConfirmsNotToProceed_JoFlagDisabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.putValue("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            DASHBOARD_SERVICE_ENABLED, false,
            JO_ONLINE_LIVE_ENABLED, false
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

        //complete the Robotics notification
        ExternalTask proccedOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proccedOffline,
            PROCESS_CASE_EVENT,
            PROCEED_OFFLINE_EVENT,
            PROCEED_OFFLINE_FOR_RESPONSE_TO_DEFENCE_ACTIVITY_ID,
            variables
        );
        //complete the notification to all parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR1_CONFIRMS_NOT_TO_PROCEED_EVENT_ID,
            variables
        );

        //complete the Robotics notification
        ExternalTask moveCaseToOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            moveCaseToOffline,
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
    void shouldSuccessfullyCompleteWhenPartPayImmediately_JoFlagEnabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.putValue("flowFlags", Map.of(
            DASHBOARD_SERVICE_ENABLED, true,
            JO_ONLINE_LIVE_ENABLED, true
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
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED,
            NOTIFY_RESPONDENT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED_ACTIVITY_ID,
            variables
        );

        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED,
            NOTIFY_APPLICANT_1_PART_ADMIT_PAY_IMMEDIATELY_AGREED_ACTIVITY_ID,
            variables
        );

        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteClaimantResponse_WhenApplicantRejectRepayment() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.putValue("flowFlags", Map.of(DASHBOARD_SERVICE_ENABLED, true));

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
            NOTIFY_EVENT,
            CLAIMANT_RESPONSE_NOT_AGREED_REPAYMENT_ACTIVITY_ID
        );

        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID,
            variables
        );
        createDefendantDashboardNotification();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"MAIN.FULL_DEFENCE_PROCEED", "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION", "MAIN.FULL_ADMIT_PROCEED",
        "MAIN.PART_ADMIT_PROCEED", "MAIN.IN_MEDIATION"})
    void shouldSuccessfullyCompleteWhenDefendantIsBilingual(String flowState) {
        // assert process has started
        assertFalse(processInstance.isEnded());

        // assert message start event
        assertThat(getProcessDefinitionByMessage("CLAIMANT_RESPONSE_SPEC").getKey())
            .isEqualTo("CLAIMANT_RESPONSE_PROCESS_ID_SPEC");

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", flowState);
        variables.putValue("flowFlags", Map.of(
            WELSH_ENABLED, true,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, true
        ));

        // complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask generateDQ = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDQ,
            PROCESS_CASE_EVENT,
            GENERATE_DIRECTIONS_QUESTIONNAIRE,
            GENERATE_DIRECTIONS_QUESTIONNAIRE_FOR_TRANSLATION_ACTIVITY_ID
        );

        // end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    private void createDefendantDashboardNotification() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE,
            CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID
        );
    }
}
