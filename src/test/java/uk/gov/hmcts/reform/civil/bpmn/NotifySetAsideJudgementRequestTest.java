package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotifySetAsideJudgementRequestTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "SET_ASIDE_JUDGMENT_REQUEST";
    public static final String PROCESS_ID = "SET_ASIDE_JUDGMENT_REQUEST";

    public NotifySetAsideJudgementRequestTest() {
        super("notify_claim_set_aside_judgement_request.bpmn", "SET_ASIDE_JUDGMENT_REQUEST");
    }

    @Test
    void shouldSuccessfullyNotifySetAsideJudgementRequest() {

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
            "NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_CLAIMANT",
            "Activity_0nyrqab"
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_DEFENDANT",
            "Activity_0txb7dk"
        );

        //complete the notification to Respondent
        ExternalTask lipRespondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            lipRespondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIM_SET_ASIDE_JUDGEMENT_LIP_LETTER_DEFENDANT1",
            "Activity_1rdnj5y"
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
