package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Respondent2TrialReadyNotifyOthersTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS";
    public static final String PROCESS_ID = "RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS";

    //CCD CASE EVENT
    public static final String RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY
        = "RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY";
    public static final String GENERATE_TRIAL_READY_FORM_RESPONDENT2
        = "GENERATE_TRIAL_READY_FORM_RESPONDENT2";

    //ACTIVITY IDs
    public static final String RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY_ACTIVITY_ID
        = "RespondentSolicitor2TrialReadyNotifierOthers";
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

        //complete the notifications
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY,
                                   RESPONDENT2_NOTIFY_OTHERS_TRIAL_READY_ACTIVITY_ID
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
