package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class CreateClaimLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "create_lip_claim.bpmn";
    private static final String MESSAGE_NAME = "CREATE_LIP_CLAIM";
    private static final String PROCESS_ID = "CREATE_LIP_CLAIM_PROCESS_ID";

    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT
        = "NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC";
    private static final String NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        = "CreateClaimContinuingOnlineNotifyApplicant1ForSpec";

    public CreateClaimLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCreateLipClaim() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        assertApplicantNotificationCompleted();
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    private void assertApplicantNotificationCompleted() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_EVENT,
            NOTIFY_APPLICANT1_FOR_CLAIM_CONTINUING_ONLINE_SPEC_ACTIVITY_ID
        );
    }
}
