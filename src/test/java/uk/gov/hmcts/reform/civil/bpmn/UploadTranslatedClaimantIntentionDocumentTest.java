package uk.gov.hmcts.reform.civil.bpmn;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

public class UploadTranslatedClaimantIntentionDocumentTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_LIP";
    private static final String SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT = "SET_SETTLEMENT_AGREEMENT_DEADLINE";
    private static final String SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID = "SetSettlementAgreementDeadline";
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT
        = "NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC";
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPRespondentClaimantConfirmToProceed";
    private static final String UPDATE_CLAIM_STATE_EVENT
        = "UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED";
    private static final String UPDATE_CLAIM_STATE_ACTIVITY_ID
        = "UpdateClaimStateAfterTranslatedDocUploaded";
    private static final String PROCEED_OFFLINE_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String PROCEED_OFFLINE_EVENT_ACTIVITY_ID = "ProceedOffline";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED
            = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
            = "NotifyRoboticsOnContinuousFeed";
    private static final String NOTIFY_JO_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
        = "NotifyJoRoboticsOnContinuousFeed";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE = "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID = "GenerateDashboardNotificationRespondent1";
    private static final String CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE
            = "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID
            = "GenerateClaimantDashboardNotificationClaimantResponse";
    private static final String SEND_JUDGMENT_DETAILS_CJES_EVENT = "SEND_JUDGMENT_DETAILS_CJES";
    private static final String SEND_JUDGMENT_DETAILS_CJES_EVENT_ID = "SendJudgmentDetailsToCJES";
    private static final String UPDATE_CLAIMANT_CLAIM_STATE_ACTIVITY_ID = "UpdateClaimStateAfterTranslatedDocUpload";
    public static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT_EVENT_ID = "GenerateClaimantCCJDashboardNotification";
    private static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT_EVENT_ID = "GenerateDefendantCCJDashboardNotification";

    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocClaimant";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocDefendant";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID = "JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID = "PostPINInLetterLIPDefendant";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String CLAIMANT_CONFIRMS_PROCEED_NOTIFY_PARTIES_ACTIVITY_ID = "ClaimantConfirmProceedNotifyParties";
    private static final String POST_CLAIMANT_LIP_JBA_LETTER_ID = "PostClaimantLIPJBALetter";
    private static final String POST_CLAIMANT_LIP_JBA_LETTER = "POST_CLAIMANT_LIP_JBA_LETTER";

    public UploadTranslatedClaimantIntentionDocumentTest() {
        super("upload_translated_claimant_intention_document_notify.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_CLAIMANT_INTENTION");
    }

    @Test
    void shouldRunProcess() {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, null);
        variables.put(FLOW_STATE, null);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the claim issue
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimIssue,
            PROCESS_CASE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID
        );

        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );

        //complete the state change task
        ExternalTask updateClaimStateTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimStateTask,
            PROCESS_CASE_EVENT,
            UPDATE_CLAIM_STATE_EVENT,
            UPDATE_CLAIM_STATE_ACTIVITY_ID
        );

        // generate dashboard notification task
        ExternalTask generateDashboardNotificationsTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDashboardNotificationsTask,
            PROCESS_CASE_EVENT,
            CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE,
            GENERATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID
        );

        //complete the Generate Dashboard Notification Respondent 1
        ExternalTask notificationTaskForDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTaskForDashboardNotification,
                                   PROCESS_CASE_EVENT,
                                   CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE,
                                   CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcessWhenFlagSet() {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of("LIP_JUDGMENT_ADMISSION", true,
                                         "JO_ONLINE_LIVE_ENABLED", false));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the claim issue
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimIssue,
            PROCESS_CASE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID
        );

        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );

        //complete the state change task
        ExternalTask proceedCaseOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedCaseOffline,
            PROCESS_CASE_EVENT,
            PROCEED_OFFLINE_EVENT,
            PROCEED_OFFLINE_EVENT_ACTIVITY_ID
        );

        ExternalTask notifyRPA = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyRPA,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //create dashboard notification
        generateClaimantLipApplicationOfflineDashboardNotification();
        generateDefendantLipApplicationOfflineDashboardNotification();
        generateClaimantDashboardNotificationForCCJClaimantResponse();
        generateDefendantDashboardNotificationForCCJClaimantResponse();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcess_ClaimIsHavingMediation(boolean welshEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.IN_MEDIATION");
        variables.put(FLOW_FLAGS, Map.of(WELSH_ENABLED, welshEnabled));
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT,
                START_BUSINESS_ACTIVITY,
                variables
        );

        //complete the claim issue
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                claimIssue,
                PROCESS_CASE_EVENT,
                SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT,
                SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID
        );

        //complete the notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        if (welshEnabled) {
            assertCompleteExternalTask(
                notificationRespondentTask,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                CLAIMANT_CONFIRMS_PROCEED_NOTIFY_PARTIES_ACTIVITY_ID
            );
        } else {
            assertCompleteExternalTask(
                notificationRespondentTask,
                PROCESS_CASE_EVENT,
                NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT,
                NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
            );
        }

        //complete the RPA notification
        ExternalTask proceedCaseOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                proceedCaseOffline,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
        );

        //complete the state change task
        ExternalTask updateClaimStateTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                updateClaimStateTask,
                PROCESS_CASE_EVENT,
                UPDATE_CLAIM_STATE_EVENT,
                UPDATE_CLAIM_STATE_ACTIVITY_ID
        );

        // generate dashboard notification task
        ExternalTask generateDashboardNotificationsTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDashboardNotificationsTask,
            PROCESS_CASE_EVENT,
            CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE,
            GENERATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID
        );

        //complete the Generate Dashboard Notification Respondent 1
        ExternalTask notificationTaskForDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTaskForDashboardNotification,
                                   PROCESS_CASE_EVENT,
                                   CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE,
                                   CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true, true", "false, false"})
    void shouldRunProcessWhenJudgementOnlineLiveEnabled(boolean isRpaLiveFeed, boolean isCjesServiceEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of("LIP_JUDGMENT_ADMISSION", true,
                                         "JO_ONLINE_LIVE_ENABLED", true,
                                         IS_JO_LIVE_FEED_ACTIVE, isRpaLiveFeed,
                                         IS_CJES_SERVICE_ENABLED, isCjesServiceEnabled));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //complete the claim issue
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimIssue,
            PROCESS_CASE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_EVENT,
            SET_SETTLEMENT_AGREEMENT_DEADLINE_ACTIVITY_ID
        );
        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );
        //complete the state change task
        ExternalTask updateClaimStateTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimStateTask,
            PROCESS_CASE_EVENT,
            UPDATE_CLAIM_STATE_EVENT,
            UPDATE_CLAIMANT_CLAIM_STATE_ACTIVITY_ID
        );

        ExternalTask sendJudgmentToCjesService = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentToCjesService,
            PROCESS_CASE_EVENT,
            SEND_JUDGMENT_DETAILS_CJES_EVENT,
            SEND_JUDGMENT_DETAILS_CJES_EVENT_ID
        );

        ExternalTask generateJudgmentByAdmissionClaimantDocument = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateJudgmentByAdmissionClaimantDocument,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID
        );

        ExternalTask generateJudgmentByAdmissionDefendantDocument = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateJudgmentByAdmissionDefendantDocument,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID
        );

        postClaimantLipJbaLetter();

        ExternalTask pinAndPostLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            pinAndPostLetter,
            PROCESS_CASE_EVENT,
            JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID,
            JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID
        );

        if (isRpaLiveFeed) {
            //complete the rpa live feed
            ExternalTask notifyRPAFeed = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyRPAFeed,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED,
                NOTIFY_JO_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
            );
        }
        //complete the Generate Dashboard Notification Claimant 1
        ExternalTask notificationClaimantTaskJoIssued = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationClaimantTaskJoIssued,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT,
            CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT_EVENT_ID
        );
        //complete the Generate Dashboard Notification Respondent 1
        ExternalTask notificationRespondentTaskJoIssued = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTaskJoIssued,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT,
            CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT_EVENT_ID
        );
        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    private void generateClaimantDashboardNotificationForCCJClaimantResponse() {
        assertCompletedCaseEvent("CREATE_CLAIMANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE", "GenerateClaimantCCJDashboardNotificationClaimantResponse");
    }

    private void generateDefendantDashboardNotificationForCCJClaimantResponse() {
        assertCompletedCaseEvent("CREATE_DEFENDANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE", "GenerateDefendantCCJDashboardNotificationForClaimantResponse");
    }

    private void postClaimantLipJbaLetter() {
        assertCompletedCaseEvent(POST_CLAIMANT_LIP_JBA_LETTER, POST_CLAIMANT_LIP_JBA_LETTER_ID);
    }

    private void generateClaimantLipApplicationOfflineDashboardNotification() {
        assertCompletedCaseEvent("CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT", "claimantLipApplicationOfflineDashboardNotification");
    }

    private void generateDefendantLipApplicationOfflineDashboardNotification() {
        assertCompletedCaseEvent("CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT", "defendantLipApplicationOfflineDashboardNotification");
    }

    private void    assertCompletedCaseEvent(String eventName, String activityId) {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            eventName,
            activityId
        );
    }
}
