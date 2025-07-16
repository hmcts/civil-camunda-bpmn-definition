package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

public class UploadTranslatedGaResponseSumDocTest extends BpmnBaseGAAfterPaymentTest {

    private static final String VARY_JUDGE_GA_BY_RESP = "VARY_JUDGE_GA_BY_RESP";
    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String  LIP_RESPONDENT = "LIP_RESPONDENT";
    private static final String TRIGGER_MAIN_CASE_ID = "TriggerMainCaseToMoveOfflineId";
    private static final String TRIGGER_MAIN_CASE_TOPIC = "processGaCaseEvent";
    private static final String TRIGGER_MAIN_CASE_EVENT = "TRIGGER_MAIN_CASE_FROM_GA";
    public static final String UPDATE_FROM_GA_CASE_EVENT = "updateFromGACaseEvent";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDraftDocToMainCaseID";
    private static final String WAIT_PDF_UPDATE_ID = "WaitCivilDraftDocumentUpdatedId";
    private static final String WAIT_PDF_UPDATE_TOPIC = "WAIT_CIVIL_DOC_UPDATED_GASPEC";
    private static final String WAIT_PDF_UPDATE_EVENT = "WAIT_GA_DRAFT";
    public static final String NOTIFY_EVENT = "processExternalCaseEventGASpec";
    private static final String APPLICATION_EVENT_GA_SPEC = "applicationEventGASpec";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";
    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";

    public UploadTranslatedGaResponseSumDocTest() {
        super("upload_translated_document_ga_summary_response_doc.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_GA_SUMMARY_RESPONSE_DOC");
    }

    @ParameterizedTest
    @CsvSource({"false, false, true", "true, true, false", "true, true, true"})
    void shouldUnPauseTheTaskAfterUploadingTranslatedDocument(boolean isVaryJudgementAppTakenOffline, boolean isLipApplicant, boolean isLipRespondent) {
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            VARY_JUDGE_GA_BY_RESP, isVaryJudgementAppTakenOffline,
            LIP_APPLICANT, isLipApplicant,
            LIP_RESPONDENT, isLipRespondent));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //Complete add pdf to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Complete add pdf to main case event
        ExternalTask waitMainCaseDocUpdated = assertNextExternalTask(WAIT_PDF_UPDATE_TOPIC);
        assertCompleteExternalTask(
            waitMainCaseDocUpdated,
            WAIT_PDF_UPDATE_TOPIC,
            WAIT_PDF_UPDATE_EVENT,
            WAIT_PDF_UPDATE_ID,
            variables
        );

        if (isVaryJudgementAppTakenOffline) {
            ExternalTask triggerMainCase = assertNextExternalTask(TRIGGER_MAIN_CASE_TOPIC);
            assertCompleteExternalTask(
                triggerMainCase,
                TRIGGER_MAIN_CASE_TOPIC,
                TRIGGER_MAIN_CASE_EVENT,
                TRIGGER_MAIN_CASE_ID,
                variables
            );

        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        if (isLipApplicant || isLipRespondent) {
            //update dashboard
            ExternalTask updateCuiClaimantDashboard = assertNextExternalTask(APPLICATION_EVENT_GA_SPEC);
            assertCompleteExternalTask(
                updateCuiClaimantDashboard,
                APPLICATION_EVENT_GA_SPEC,
                UPDATE_CLAIMANT_DASHBOARD_GA_EVENT,
                GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID,
                variables
            );

            ExternalTask updateCuiDefendantDashboard = assertNextExternalTask(APPLICATION_EVENT_GA_SPEC);
            assertCompleteExternalTask(
                updateCuiDefendantDashboard,
                APPLICATION_EVENT_GA_SPEC,
                UPDATE_RESPONDENT_DASHBOARD_GA_EVENT,
                GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID,
                variables
            );
        }

        assertNoExternalTasksLeft();
    }

}
