package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GenerateTrialReadyDocumentRespondent1Test extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1";
    private static final String PROCESS_ID = "GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1";

    private static final String DASHBOARD_NOTIFICATION_EVENT = "DASHBOARD_NOTIFICATION_EVENT";
    private static final String GENERATE_TRIAL_READY_FORM_RESPONDENT1_ACTIVITY_ID = "GenerateTrialReadyFormRespondent1";

    public GenerateTrialReadyDocumentRespondent1Test() {
        super("generate_trial_ready_document_respondent1.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyFormAndNotifyDefendantsHearing() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY);

        //complete the dashboard notification
        ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(dashboardNotification,
                                   PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_TRIAL_READY_FORM_RESPONDENT1_ACTIVITY_ID);

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
