package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CreateClaimLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "create_lip_claim.bpmn";
    private static final String MESSAGE_NAME = "CREATE_LIP_CLAIM";
    private static final String PROCESS_ID = "CREATE_LIP_CLAIM_PROCESS_ID";

    //Assigning claim to applicant 1
    private static final String ASSIGN_CASE_TO_APPLICANT1_EVENT = "ASSIGN_CASE_TO_APPLICANT1";

    private static final String CREATE_SERVICE_REQUEST_CUI_EVENT = "CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE";
    private static final String ASSIGN_CASE_TO_APPLICANT1_ACTIVITY_ID = "CaseAssignmentToApplicant1";
    private static final String CREATE_SERVICE_REQUEST_CUI_ACTIVITY_ID = "CreateServiceRequestCUI";
    private static final String GENERATE_PDF_FORM_EVENT = "GENERATE_DRAFT_FORM";
    private static final String GENERATE_PDF_FORM_ACTIVITY_ID = "GenerateDraftForm";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CUI_EVENT = "GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CLAIMANT1";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CUI_ACTIVITY_ID = "GenerateDashboardNotificationClaimFeeRequired";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1 = "GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1";
    private static final String GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1_ACTIVITY_ID = "GenerateDashboardNotificationClaimIssueHwfClaimant1";

    //Notify applicant 1 claim submitted
    private static final String NOTIFY_APPLICANT1_CLAIM_SUBMITTED_EVENT = "NOTIFY_APPLICANT1_CLAIM_SUBMITTED";
    private static final String NOTIFY_APPLICANT1_CLAIM_SUBMITTED__ACTIVITY_ID = "NotifyApplicant1ClaimSubmitted";

    public CreateClaimLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCreateLipClaim() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
                "CLAIM_ISSUE_HWF", false));
        startBusinessProcess(variables);
        completeClaimIssue(variables);
        notifyApplicant1ClaimSubmitted(variables);
        generateDraftForm(variables);
        createServiceRequestCui(variables);
        generateDashboardNotificationClaimFeeRequired(variables);
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }


    @Test
    void shouldPauseServiceRequestApiCall_WhenHwFApplied() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
                "CLAIM_ISSUE_HWF", true));
        startBusinessProcess(variables);
        completeClaimIssue(variables);
        notifyApplicant1ClaimSubmitted(variables);
        generateDraftForm(variables);
        generateDashboardNotificationHwfRequested(variables);
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    private void completeClaimIssue(final VariableMap variables) {

        //complete the applicant assignment
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            ASSIGN_CASE_TO_APPLICANT1_EVENT,
            ASSIGN_CASE_TO_APPLICANT1_ACTIVITY_ID,
            variables
        );
    }

    private void notifyApplicant1ClaimSubmitted(VariableMap variables) {
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimIssue,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT1_CLAIM_SUBMITTED_EVENT,
            NOTIFY_APPLICANT1_CLAIM_SUBMITTED__ACTIVITY_ID,
            variables
        );
    }

    private void createServiceRequestCui(final VariableMap variables) {

        //complete the applicant assignment
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            CREATE_SERVICE_REQUEST_CUI_EVENT,
            CREATE_SERVICE_REQUEST_CUI_ACTIVITY_ID,
            variables
        );
    }

    private void generateDraftForm(final VariableMap variables) {
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            GENERATE_PDF_FORM_EVENT,
            GENERATE_PDF_FORM_ACTIVITY_ID,
            variables
        );
    }

    private void generateDashboardNotificationClaimFeeRequired(final VariableMap variables) {
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CUI_EVENT,
            GENERATE_DASHBOARD_NOTIFICATION_CLAIM_FEE_REQUIRED_CUI_ACTIVITY_ID,
            variables
        );
    }

    private void generateDashboardNotificationHwfRequested(final VariableMap variables) {
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1,
            GENERATE_DASHBOARD_NOTIFICATION_CLAIM_ISSUE_HWF_CLAIMANT1_ACTIVITY_ID,
            variables
        );
    }
}
