package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSDOTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CREATE_SDO";
    public static final String PROCESS_ID = "CREATE_SDO";

    public CreateSDOTest() {
        super("create_sdo.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTakeCaseOffline() {
        // assert process has started
        assertFalse(processInstance.isEnded());

        // assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        // complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        // complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor"
        );

        // complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1"
        );

        // complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2"
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
