package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UploadTranslatedClaimantsRejectsRepaymentPlanDocumentTest extends BpmnBaseTest {

    private static final String PROCESS_ID = "UPLOAD_TRANSLATED_CLAIMANTS_DOCUMENT_PROCESS_ID";
    private static final String NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT
        = "NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT";

    private static final String NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT
        = "NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT";
    private static final String NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT_ACTIVITY_ID
        = "ClaimantDisAgreedRepaymentPlanNotifyLip";

    private static final String NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT_ACTIVITY_ID
        = "ClaimantDisAgreeRepaymentPlanNotifyApplicant";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocClaimant";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocDefendant";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID = "JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID = "PostPINInLetterLIPDefendant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT_EVENT_ID = "GenerateDefendantCCJDashboardNotification";
    private static final String SEND_JUDGMENT_DETAILS_CJES_EVENT = "SEND_JUDGMENT_DETAILS_CJES";
    private static final String SEND_JUDGMENT_DETAILS_CJES_EVENT_ID = "SendJudgmentDetailsToCJES";
    private static final String UPDATE_CLAIMANT_CLAIM_STATE_ACTIVITY_ID = "UpdateClaimStateAfterTranslatedDocUploaded";
    public static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_CLAIMANT_EVENT_ID = "GenerateClaimantCCJDashboardNotification";
    private static final String UPDATE_CLAIM_STATE_EVENT
        = "UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED
        = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_JO_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID
        = "NotifyJoRoboticsOnContinuousFeed";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE = "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";
    private static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE_EVENT_ID = "GenerateDashboardNotificationRespondent1";
    private static final String CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE
        = "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_ACTIVITY_ID
        = "GenerateClaimantDashboardNotificationClaimantResponse";
    private static final String UPDATE_CLAIMANT_STATE_ACTIVITY_ID = "UpdateClaimStateAfterTranslatedDocUpload";



    public UploadTranslatedClaimantsRejectsRepaymentPlanDocumentTest() {
        super(
            "upload_translated_document_claimant_rejects_repayment_plan.bpmn",
            PROCESS_ID
        );
    }

    private void notifyClaimantClaimantRejectRepayment() {
        assertCompletedCaseEvent(NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT, NOTIFY_CLAIMANT_FOR_RESPONDENT1_REJECT_REPAYMENT_ACTIVITY_ID);
    }

    private void notifyRespondentClaimantRejectRepayment() {
        assertCompletedCaseEvent(NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT, NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT_ACTIVITY_ID);
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

    @Test
    void shouldRunProcess_ClaimIsInFullAdmitRejectRepaymentUploadDocuments() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of("LIP_JUDGMENT_ADMISSION", false));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        notifyRespondentClaimantRejectRepayment();
        notifyClaimantClaimantRejectRepayment();

        //complete the state change task
        ExternalTask updateClaimStateTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimStateTask,
            PROCESS_CASE_EVENT,
            UPDATE_CLAIM_STATE_EVENT,
            UPDATE_CLAIMANT_CLAIM_STATE_ACTIVITY_ID
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
    @ValueSource(strings = {"true", "false"})
    void shouldRunProcessWhenJudgementOnlineLiveEnabled(boolean isRpaLiveFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_AGREE_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of("LIP_JUDGMENT_ADMISSION", true,
                                         "JO_ONLINE_LIVE_ENABLED", true,
                                         IS_JO_LIVE_FEED_ACTIVE, isRpaLiveFeed));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        notifyRespondentClaimantRejectRepayment();
        notifyClaimantClaimantRejectRepayment();

        //complete the state change task
        ExternalTask updateClaimStateTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimStateTask,
            PROCESS_CASE_EVENT,
            UPDATE_CLAIM_STATE_EVENT,
            UPDATE_CLAIMANT_STATE_ACTIVITY_ID
        );
        //Send judgement details to CJES service
        ExternalTask sendJudgmentToCjesService = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentToCjesService,
            PROCESS_CASE_EVENT,
            SEND_JUDGMENT_DETAILS_CJES_EVENT,
            SEND_JUDGMENT_DETAILS_CJES_EVENT_ID
        );

        ExternalTask generateJudgmentByAdmissionClaimantDocument = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentToCjesService,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID
        );

        ExternalTask generateJudgmentByAdmissionDefendantDocument = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentToCjesService,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID
        );

        ExternalTask pinAndPostLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentToCjesService,
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
}
