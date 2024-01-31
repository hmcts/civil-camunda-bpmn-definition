package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

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

    //Activity IDs
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPRespondentClaimantConfirmToProceed";
    private static final String DQ_PDF_ACTIVITY_ID = "Generate_LIP_Claimant_DQ";
    private static final String DQ_PDF_EVENT = "GENERATE_RESPONSE_DQ_LIP_SEALED";

    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPApplicantClaimantConfirmToProceed";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
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
            IS_MULTI_TRACK, true
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
        generateDQPdf();
        updateClaimState();
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
                "LIP_JUDGMENT_ADMISSION", true
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
        notifyRPACaseHandledOffline();
        updateClaimState();
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
                "LIP_JUDGMENT_ADMISSION", true
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
        notifyRPACaseHandledOffline();
        updateClaimState();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void assertProcessHasStarted() {
        assertFalse(processInstance.isEnded());
    }

    @Test
    void shouldRunProcess_ClaimIsInFullAdmitRejectRepayment() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
                "LIP_JUDGMENT_ADMISSION", false
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
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInPartAdmitRejectPayment() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_REJECT_REPAYMENT");
        variables.put(FLOW_FLAGS, Map.of(
                "LIP_JUDGMENT_ADMISSION", false
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
            IS_MULTI_TRACK, true
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
        generateDQPdf();
        updateClaimState();
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
                "LIP_JUDGMENT_ADMISSION", false
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
}
