package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ClaimantResponseCuiTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "CLAIMANT_RESPONSE_CUI";
    private static final String PROCESS_ID = "CLAIMANT_RESPONSE_CUI_PROCESS_ID";
    private static final String JUDICIAL_REFERRAL_EVENT = "JUDICIAL_REFERRAL";
    private static final String JUDICIAL_REFERRAL_ACTIVITY_ID = "JudicialReferral";
    private static final String JUDICIAL_REFERRAL_FULL_DEFENCE_ACTIVITY_ID = "Judicial_Referral";
    //CCD Case Event
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED
        = "NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED";

    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED
        = "NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED";

    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED
        = "NOTIFY_RPA_ON_CONTINUOUS_FEED";

    private static final String NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT
        = "NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT";

    private static final String NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT
        = "NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT";

    private static final String GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC
        = "GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC";

    private static final String GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC
        = "GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC";

    private static final String CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE
        = "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";

    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT";

    //Activity IDs
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPRespondentClaimantConfirmToProceed";
    private static final String TRIGGER_UPDATE_GA_LOCATION = "TRIGGER_UPDATE_GA_LOCATION";
    private static final String TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID = "TriggerAndUpdateGenAppLocation";
    private static final String DQ_PDF_ACTIVITY_ID = "Generate_LIP_Claimant_DQ";
    private static final String DQ_PDF_EVENT = "GENERATE_RESPONSE_DQ_LIP_SEALED";

    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPApplicantClaimantConfirmToProceed";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String NOTIFY_JO_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyJoRoboticsOnContinuousFeed";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    private static final String NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT_ACTIVITY_ID
        = "ClaimantDisAgreedRepaymentPlanNotifyLip";

    private static final String NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT_ACTIVITY_ID
        = "ClaimantDisAgreeRepaymentPlanNotifyApplicant";
    private static final String LIP_CLAIMANT_MD_ACTIVITY_ID = "Generate_LIP_Claimant_MD";
    private static final String LIP_CLAIMANT_MD = "GENERATE_LIP_CLAIMANT_MANUAL_DETERMINATION";
    private static final String UPDATE_CLAIMANT_INTENTION_CLAIM_STATE_EVENT = "UPDATE_CLAIMANT_INTENTION_CLAIM_STATE";
    private static final String UPDATE_CLAIMANT_INTENTION_CLAIM_STATE_EVENT_ID = "updateClaimantIntentionClaimStateID";
    private static final String GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT
        = "GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT";
    private static final String GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT_ACTIVITY_ID
        = "GenerateInterlocutoryJudgementDocument";
    private static final String GENERATE_JUDGMENT_BY_ADMISSION_PDF_ACTIVITY_ID
        = "GenerateJudgmentByAdmissionPdf";
    private static final String GENERATE_JUDGMENT_BY_DETERMINATION_PDF_ACTIVITY_ID
        = "GenerateJudgmentByDeterminationPdf";
    private static final String PROCEED_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String PROCEED_OFFLINE_EVENT_ACTIVITY_ID = "ProceedOffline";
    private static final String SEND_JUDGMENT_DETAILS_CJES_EVENT = "SEND_JUDGMENT_DETAILS_CJES";
    private static final String SEND_JUDGMENT_DETAILS_CJES_EVENT_ID = "SendJudgmentDetailsToCJES";
    private static final String UPDATE_CLAIMANT_CLAIM_STATE_EVENT_ID = "updateClaimantClaimStateID";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE = "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID = "GenerateDashboardNotificationRespondent1";
    private static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT_EVENT_ID = "GenerateDefendantCCJDashboardNotification";
    public static final String DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT";
    public static final String DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT_EVENT_ID = "GenerateClaimantCCJDashboardNotification";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID
        = "GenerateClaimantDashboardNotificationClaimantResponse";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocClaimant";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocDefendant";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID = "JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID = "PostPINInLetterLIPDefendant";

    public ClaimantResponseCuiTest() {
        super(
            "claimant_response_cui.bpmn",
            "CLAIMANT_RESPONSE_CUI_PROCESS_ID"
        );
    }

    @Test
    void shouldRunProcess() {
        //Given
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            GENERAL_APPLICATION_ENABLED, true,
            IS_MULTI_TRACK, true,
            CLAIM_ISSUE_BILINGUAL, false
        ));

        //Then
        assertProcessHasStarted();
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        startBusinessProcess(variables);
        assertCompletedCaseEvent(
            JUDICIAL_REFERRAL_EVENT,
            JUDICIAL_REFERRAL_ACTIVITY_ID,
            variables
        );

        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        assertCompletedCaseEvent(
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInMediation() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.IN_MEDIATION");
        variables.put(FLOW_FLAGS, Map.of(
            CLAIM_ISSUE_BILINGUAL, false
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        generateRPAContinuousFeed();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInFullAdmitRepaymentAccept() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
                LIP_JUDGMENT_ADMISSION, true,
                CLAIM_ISSUE_BILINGUAL, false
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        generateJudgmentByAdmissionPdf();
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        proceedCaseOffline();
        notifyRPACaseHandledOffline();
        generateClaimantDashboardNotificationForCCJClaimantResponse();
        generateDefendantDashboardNotificationForCCJClaimantResponse();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInPartAdmitRepaymentAccept() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_AGREE_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
                LIP_JUDGMENT_ADMISSION, true,
                CLAIM_ISSUE_BILINGUAL, false,
                JO_ONLINE_LIVE_ENABLED, false
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        generateJudgmentByAdmissionPdf();
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        proceedCaseOffline();
        notifyRPACaseHandledOffline();
        generateClaimantDashboardNotificationForCCJClaimantResponse();
        generateDefendantDashboardNotificationForCCJClaimantResponse();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void assertProcessHasStarted() {
        assertFalse(processInstance.isEnded());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcess_ClaimIsInFullAdmitRejectRepayment(boolean joEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
                LIP_JUDGMENT_ADMISSION, false,
                CLAIM_ISSUE_BILINGUAL, false,
                JO_ONLINE_LIVE_ENABLED, joEnabled
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        notifyRespondentClaimantRejectRepayment();
        notifyClaimantClaimantRejectRepayment();
        generateManualDeterminationPdf();
        requestInterlockJudgement();
        generateJudgmentByDeterminationPdf();
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,true"})
    void shouldRunProcess_ClaimIsInFullAdmitRejectRepaymentAndWelshParty(boolean isRespondentBilingual, boolean isClaimantBilingual) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, false,
            CLAIM_ISSUE_BILINGUAL, isClaimantBilingual,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, isRespondentBilingual,
            WELSH_ENABLED, true,
            JO_ONLINE_LIVE_ENABLED, false));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        generateManualDeterminationPdf();
        requestInterlockJudgement();
        generateJudgmentByDeterminationPdf();
        generateDQPdf();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true,false", "true, true"})
    void shouldRunProcess_ClaimIsInFullAdmitRejectRepaymentAndWelshFTisOff(boolean isClaimantBilingual, boolean isRespondentBilingual) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, false,
            CLAIM_ISSUE_BILINGUAL, isClaimantBilingual,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, isRespondentBilingual,
            WELSH_ENABLED, false,
            JO_ONLINE_LIVE_ENABLED, false));

        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        notifyRespondentClaimantRejectRepayment();
        notifyClaimantClaimantRejectRepayment();
        generateManualDeterminationPdf();
        requestInterlockJudgement();
        generateJudgmentByDeterminationPdf();
        generateDQPdf();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcess_ClaimIsInPartAdmitRejectPayment(boolean joEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
                LIP_JUDGMENT_ADMISSION, false,
                CLAIM_ISSUE_BILINGUAL, false,
                JO_ONLINE_LIVE_ENABLED, joEnabled
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        notifyRespondentClaimantRejectRepayment();
        notifyClaimantClaimantRejectRepayment();
        generateManualDeterminationPdf();
        requestInterlockJudgement();
        generateJudgmentByDeterminationPdf();
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimFullDefenceNotAgreeMediation() {
        //Given
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            GENERAL_APPLICATION_ENABLED, true,
            IS_MULTI_TRACK, true,
            CLAIM_ISSUE_BILINGUAL, false
        ));

        //Then
        assertProcessHasStarted();
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        startBusinessProcess(variables);
        assertCompletedCaseEvent(
            JUDICIAL_REFERRAL_EVENT,
            JUDICIAL_REFERRAL_FULL_DEFENCE_ACTIVITY_ID,
            variables
        );

        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        assertCompletedCaseEvent(
            TRIGGER_UPDATE_GA_LOCATION,
            TRIGGER_UPDATE_GA_LOCATION_ACTIVITY_ID,
            variables
        );
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInPartAdmitPayImmediately() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.put(FLOW_FLAGS, Map.of(
                LIP_JUDGMENT_ADMISSION, false,
                CLAIM_ISSUE_BILINGUAL, false
        ));
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInPartAdmitPayImmediately_whenDefendantBilingual_EnglishToWelshDisabled() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, false,
            CLAIM_ISSUE_BILINGUAL, false,
            RESPONDENT_BILINGUAL, true
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInPartAdmitPayImmediately_whenDefendantBilingual_EnglishToWelshEnabled() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, false,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, true,
            CLAIM_ISSUE_BILINGUAL, false,
            WELSH_ENABLED, true
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInPartAdmitPayImmediately_whenBothBilingual_EnglishToWelshEnabled() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, false,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, false,
            CLAIM_ISSUE_BILINGUAL, true,
            WELSH_ENABLED, false
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIssueInBilingual() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        variables.put(FLOW_FLAGS, Map.of(
            CLAIM_ISSUE_BILINGUAL, true
        ));
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_whenFullDefenceWithRejectAll() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_NOT_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
                LIP_JUDGMENT_ADMISSION, false,
                CLAIM_ISSUE_BILINGUAL, false,
                JO_ONLINE_LIVE_ENABLED, false
        ));
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_whenFullDefenceNotProceedAndDefendantNocOnlineEnabled() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_NOT_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, false,
            CLAIM_ISSUE_BILINGUAL, false,
            JO_ONLINE_LIVE_ENABLED, false,
            DEFENDANT_NOC_ONLINE, true
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        updateClaimState();
        createClaimantDashboardNotification();
        createDefendantDashboardNotification();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcess_ClaimIsInFullAdmitRepaymentAcceptedAndJudgmentOnlineLive(boolean isRpaLiveFeed) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, true,
            CLAIM_ISSUE_BILINGUAL, false,
            JO_ONLINE_LIVE_ENABLED, true,
            IS_JO_LIVE_FEED_ACTIVE, isRpaLiveFeed
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        generateJudgmentByAdmissionPdf();
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        updateClaimantClaimState();
        sendJudgmentToCjesService();
        generateJudgmentByAdmissionClaimantDocument();
        generateJudgmentByAdmissionDefendantDocument();
        sendPinInPOstLetterForJudgmentByAdmission();
        if (isRpaLiveFeed) {
            generateJoRPAContinuousFeed();
        }
        createClaimantDashboardNotificationForJOIssued();
        createDefendantDashboardNotificationForJOIssued();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcess_ClaimIsInFullAdmitRepaymentRejectedJBAAndJudgmentOnlineLive(boolean isRpaLiveFeed) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, true,
            CLAIM_ISSUE_BILINGUAL, false,
            JO_ONLINE_LIVE_ENABLED, true,
            IS_JO_LIVE_FEED_ACTIVE, isRpaLiveFeed
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        generateJudgmentByAdmissionPdf();
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        updateClaimantClaimState();
        sendJudgmentToCjesService();
        generateJudgmentByAdmissionClaimantDocument();
        generateJudgmentByAdmissionDefendantDocument();
        sendPinInPOstLetterForJudgmentByAdmission();
        if (isRpaLiveFeed) {
            generateJoRPAContinuousFeed();
        }
        createClaimantDashboardNotificationForJOIssued();
        createDefendantDashboardNotificationForJOIssued();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcess_ClaimIsInPartAdmitRepaymentRejectedJBAAndJudgmentOnlineLive(boolean isRpaLiveFeed) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_JUDGMENT_ADMISSION, true,
            CLAIM_ISSUE_BILINGUAL, false,
            JO_ONLINE_LIVE_ENABLED, true,
            IS_JO_LIVE_FEED_ACTIVE, isRpaLiveFeed
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        generateJudgmentByAdmissionPdf();
        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        generateDQPdf();
        updateClaimantClaimState();
        sendJudgmentToCjesService();
        generateJudgmentByAdmissionClaimantDocument();
        generateJudgmentByAdmissionDefendantDocument();
        sendPinInPOstLetterForJudgmentByAdmission();
        if (isRpaLiveFeed) {
            generateJoRPAContinuousFeed();
        }
        createClaimantDashboardNotificationForJOIssued();
        createDefendantDashboardNotificationForJOIssued();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void generateJudgmentByAdmissionPdf() {
        assertCompletedCaseEvent(GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, GENERATE_JUDGMENT_BY_ADMISSION_PDF_ACTIVITY_ID);
    }

    private void generateJudgmentByDeterminationPdf() {
        assertCompletedCaseEvent(GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC, GENERATE_JUDGMENT_BY_DETERMINATION_PDF_ACTIVITY_ID);
    }

    private void notifyRespondentClaimantConfirmsToProceed() {
        assertCompletedCaseEvent(NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED, NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID);
    }

    private void createClaimantDashboardNotification() {
        assertCompletedCaseEvent(CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE, GENERATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID);
    }

    private void notifyClaimantClaimantRejectRepayment() {
        assertCompletedCaseEvent(NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT, NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT_ACTIVITY_ID);
    }

    private void notifyRespondentClaimantRejectRepayment() {
        assertCompletedCaseEvent(NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT, NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT_ACTIVITY_ID);
    }

    private void generateDQPdf() {
        assertCompletedCaseEvent(DQ_PDF_EVENT, DQ_PDF_ACTIVITY_ID);
    }

    private void generateRPAContinuousFeed() {
        assertCompletedCaseEvent(NOTIFY_RPA_ON_CONTINUOUS_FEED, NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID);
    }

    private void generateManualDeterminationPdf() {
        assertCompletedCaseEvent(LIP_CLAIMANT_MD, LIP_CLAIMANT_MD_ACTIVITY_ID);
    }

    private void updateClaimState() {
        assertCompletedCaseEvent(UPDATE_CLAIMANT_INTENTION_CLAIM_STATE_EVENT, UPDATE_CLAIMANT_INTENTION_CLAIM_STATE_EVENT_ID);
    }

    private void createDefendantDashboardNotification() {
        assertCompletedCaseEvent(CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE, CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID);
    }

    private void assertCompletedCaseEvent(String eventName, String activityId) {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            eventName,
            activityId
        );
    }

    private void assertCompletedCaseEvent(String eventName, String activityId, VariableMap variables) {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            eventName,
            activityId,
            variables
        );
    }

    private void notifyApplicantClaimantConfirmsToProceed() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED,
            NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );
    }

    private void requestInterlockJudgement() {
        assertCompletedCaseEvent(
            GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT,
            GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT_ACTIVITY_ID
        );
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

    private void notifyRPACaseHandledOffline() {
        assertCompletedCaseEvent(NOTIFY_RPA_ON_CASE_HANDED_OFFLINE, NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID);
    }

    private void generateClaimantDashboardNotificationForCCJClaimantResponse() {
        assertCompletedCaseEvent("CREATE_CLAIMANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE", "GenerateClaimantCCJDashboardNotificationClaimantResponse");
    }

    private void generateDefendantDashboardNotificationForCCJClaimantResponse() {
        assertCompletedCaseEvent("CREATE_DEFENDANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE", "GenerateDefendantCCJDashboardNotificationForClaimantResponse");
    }

    private void proceedCaseOffline() {
        assertCompletedCaseEvent(PROCEED_OFFLINE_EVENT, PROCEED_OFFLINE_EVENT_ACTIVITY_ID);
    }

    private void sendJudgmentToCjesService() {
        assertCompletedCaseEvent(SEND_JUDGMENT_DETAILS_CJES_EVENT, SEND_JUDGMENT_DETAILS_CJES_EVENT_ID);
    }

    private void generateJudgmentByAdmissionClaimantDocument() {
        assertCompletedCaseEvent(GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT, GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID);
    }

    private void generateJudgmentByAdmissionDefendantDocument() {
        assertCompletedCaseEvent(GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT, GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID);
    }

    private void sendPinInPOstLetterForJudgmentByAdmission() {
        assertCompletedCaseEvent(JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID, JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID);
    }

    private void updateClaimantClaimState() {
        assertCompletedCaseEvent(UPDATE_CLAIMANT_INTENTION_CLAIM_STATE_EVENT, UPDATE_CLAIMANT_CLAIM_STATE_EVENT_ID);
    }

    private void createDefendantDashboardNotificationForJOIssued() {
        assertCompletedCaseEvent(CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT_EVENT_ID);
    }

    private void createClaimantDashboardNotificationForJOIssued() {
        assertCompletedCaseEvent(DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT, DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT_EVENT_ID);
    }

    private void generateJoRPAContinuousFeed() {
        assertCompletedCaseEvent(NOTIFY_RPA_ON_CONTINUOUS_FEED, NOTIFY_JO_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID);
    }
}
