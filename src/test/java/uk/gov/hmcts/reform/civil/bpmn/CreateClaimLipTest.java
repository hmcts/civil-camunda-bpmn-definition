package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class CreateClaimLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "create_lip_claim.bpmn";
    private static final String MESSAGE_NAME = "CREATE_LIP_CLAIM";
    private static final String PROCESS_ID = "CREATE_LIP_CLAIM_PROCESS_ID";

    private static final String PROCESS_CLAIM_ISSUE_EVENT = "PROCESS_CLAIM_ISSUE_SPEC";
    private static final String PROCESS_CLAIM_ISSUE_ACTIVITY_ID = "IssueClaimForSpec";

    //Assigning claim to applicant 1
    private static final String ASSIGN_CASE_TO_APPLICANT1_EVENT = "ASSIGN_CASE_TO_APPLICANT1";
    private static final String ASSIGN_CASE_TO_APPLICANT1_ACTIVITY_ID = "CaseAssignmentToApplicant1";

    //notify applicant 1
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";

    //notify respondent 1
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyRespondent1ForSpec";

    public CreateClaimLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCreateLipClaim() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        completeClaimIssue(variables);
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

        //complete the claim issue
        ExternalTask claimIssue = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimIssue,
            PROCESS_CASE_EVENT,
            PROCESS_CLAIM_ISSUE_EVENT,
            PROCESS_CLAIM_ISSUE_ACTIVITY_ID,
            variables
        );

        //complete the applicant notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
            NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        );

        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
            NOTIFY_RESPONDENT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        );
    }

}
