package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class BreathingSpaceLiftedLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "breathing_space_lifted_lip.bpmn";
    private static final String MESSAGE_NAME = "LIFT_BREATHING_SPACE_LIP";
    private static final String PROCESS_ID = "LIFT_BREATHING_SPACE_LIP";

    //CCD Case Event
    private static final String NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED
        = "NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED";
    private static final String NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED
        = "NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED";
    //Activity IDs
    private static final String NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED_ID
        = "NotifyApplicantBreathingSpaceLifted";
    private static final String NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED_ID
        = "NotifyRespondentBreathingSpaceLifted";

    public BreathingSpaceLiftedLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullySubmitBreathingSpaceLifted() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        notifyApplicantBreathingSpaceLifted();
        notifyRespondentBreathingSpaceLifted();
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    private void notifyApplicantBreathingSpaceLifted() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED,
            NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED_ID
        );
    }

    private void notifyRespondentBreathingSpaceLifted() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED,
            NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED_ID
        );
    }
}
