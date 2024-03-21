package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotifyJudgmentVariedDeterminationOfMeansTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS";
    public static final String PROCESS_ID = "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS";

    public NotifyJudgmentVariedDeterminationOfMeansTest() {
        super("notify_judgment_varied_determination_of_means.bpmn.bpmn", "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS");
    }

    @Test
    void shouldSuccessfullyNotifyDecisionOnReconsiderationRequest() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS",
            "Activity_0nyrqab"
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT",
            "Activity_0txb7dk"
        );

        //complete the claim form for reconsideration generation
        ExternalTask generateClaimForm = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateClaimForm,
            PROCESS_CASE_EVENT,
            "NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS",
            "Activity_06y8ktx"
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
