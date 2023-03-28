package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Respondent2TrialReadyNotifyOthersTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "TRIAL_READY_NOTIFICATION";
    public static final String PROCESS_ID = "RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS";

    //CCD CASE EVENT
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY";
    public static final String NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY
        = "NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY";
    public static final String GENERATE_TRIAL_READY_FORM_RESPONDENT2
        = "GENERATE_TRIAL_READY_FORM_RESPONDENT2";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY_ACTIVITY_ID
        = "OtherTrialReadyNotifyRespondentSolicitor1";
    private static final String NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY_ACTIVITY_ID
        = "OtherTrialReadyNotifyApplicantSolicitor1";
    public static final String GENERATE_TRIAL_READY_FORM_RESPONDENT2_ACTIVITY_ID
        = "GenerateTrialReadyFormRespondent2";

    public Respondent2TrialReadyNotifyOthersTest() {
        super("respondent2_trial_ready_notify_others.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyFormAndNotifyClaimantAndDefendantHearing() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        ExternalTask notificationTask;

        //complete the defendant2 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY_ACTIVITY_ID
        );



        //complete the applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY,
                                   NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY_ACTIVITY_ID
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT2,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT2_ACTIVITY_ID
        );


        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
