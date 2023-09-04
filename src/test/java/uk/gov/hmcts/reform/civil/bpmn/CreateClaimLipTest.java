package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class CreateClaimLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "create_lip_claim.bpmn";
    private static final String MESSAGE_NAME = "CREATE_LIP_CLAIM";
    private static final String PROCESS_ID = "CREATE_LIP_CLAIM_PROCESS_ID";

    private static final String ASSIGN_CASE_TO_APPLICANT1_EVENT = "ASSIGN_CASE_TO_APPLICANT1";
    private static final String ASSIGN_CASE_TO_APPLICANT1_ACTIVITY_ID = "CaseAssignmentToApplicant1";

    private static final String GENERATE_PDF_FORM_EVENT = "GENERATE_DRAFT_FORM";
    private static final String GENERATE_PDF_FORM_ACTIVITY_ID = "GenerateDraftForm";

    public CreateClaimLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCreateLipClaim() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        completeApplicantAssignment(variables);
        generateDraftForm(variables);
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    private void completeApplicantAssignment(final VariableMap variables) {
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            ASSIGN_CASE_TO_APPLICANT1_EVENT,
            ASSIGN_CASE_TO_APPLICANT1_ACTIVITY_ID,
            variables
        );
    }

    private void generateDraftForm (final VariableMap variables) {
        ExternalTask assignTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            assignTask,
            PROCESS_CASE_EVENT,
            GENERATE_PDF_FORM_EVENT,
            GENERATE_PDF_FORM_ACTIVITY_ID,
            variables
        );
    }
}
