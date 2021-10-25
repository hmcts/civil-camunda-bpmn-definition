package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CaseProceedsInCasemanTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CASE_PROCEEDS_IN_CASEMAN";
    public static final String PROCESS_ID = "CASE_PROCEEDS_IN_CASEMAN";
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM_EVENT = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID = "ProceedOffline";

    public CaseProceedsInCasemanTest() {
        super("case_proceeds_in_caseman.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteNotifyClaim_whenCalled() {
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

        //Take offline
        ExternalTask takeOffline = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            takeOffline,
            PROCESS_CASE_EVENT,
            PROCEEDS_IN_HERITAGE_SYSTEM_EVENT,
            PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete the RPA notification
        ExternalTask rpaNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            rpaNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE",
            "NotifyRoboticsOnCaseHandedOffline"
        );

        //complete the notification to respondent
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN",
                                   "CaseProceedsInCasemanNotifyRespondentSolicitor1"
        );

        //complete the notification to applicant
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_PROCEEDS_IN_CASEMAN",
                                   "CaseProceedsInCasemanNotifyApplicantSolicitor1"
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
