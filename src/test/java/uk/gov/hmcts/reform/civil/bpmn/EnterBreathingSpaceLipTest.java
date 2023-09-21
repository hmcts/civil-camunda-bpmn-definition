package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class EnterBreathingSpaceLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "enter_breathing_space_lip.bpmn";
    private static final String MESSAGE_NAME = "ENTER_BREATHING_SPACE_LIP";
    private static final String PROCESS_ID = "ENTER_BREATHING_SPACE_PROCESS_ID_LIP";

    //CCD Case Event
    private static final String NOTIFY_APPLICANT1_BREATHING_SPACE_LIP
        = "NOTIFY_LIP_APPLICANT1_BREATHING_SPACE_ENTER";
    private static final String NOTIFY_RESPONDENT_BREATHING_SPACE_LIP
        = "NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_ENTER";
    //Activity IDs
    private static final String NOTIFY_APPLICANT1_BREATHING_SPACE_LIP_ID
        = "NotifyApplicant1BreathingSpaceLIP";
    private static final String NOTIFY_RESPONDENT_BREATHING_SPACE_LIP_ID
        = "NotifyRespondentBreathingSpaceLIP";
    public EnterBreathingSpaceLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullySubmitBreathingSpace() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        startBusinessProcess(variables);
        notifyApplicant1BreathingSpaceLip();
        notifyRespondentBreathingSpaceLip();
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
    private void notifyApplicant1BreathingSpaceLip() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT1_BREATHING_SPACE_LIP,
            NOTIFY_APPLICANT1_BREATHING_SPACE_LIP_ID
        );
    }
    private void notifyRespondentBreathingSpaceLip() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_BREATHING_SPACE_LIP,
            NOTIFY_RESPONDENT_BREATHING_SPACE_LIP_ID
        );
    }
}
