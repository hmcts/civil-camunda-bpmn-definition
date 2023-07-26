package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RequestJudgementByAdmissionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "REQUEST_JUDGEMENT_ADMISSION_SPEC";
    public static final String PROCESS_ID = "REQUEST_JUDGEMENT_ADMISSION_SPEC_ID";

    //CCD CASE EVENTS
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM
        = "PROCEEDS_IN_HERITAGE_SYSTEM";
    public static final String NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION
        = "NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION";
    public static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE
        = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";

    //ACTIVITY IDs
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        = "proceedsInHeritageSystem";
    public static final String NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_ACTIVITY_ID
        = "RequestJudgementByAdmissionNotifyRespondent1";
    public static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";

    public RequestJudgementByAdmissionTest() {
        super("request_judgement_by_admission.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteBreathingSpaceLifted_withRpa() {

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //complete the proceed offline
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   PROCEEDS_IN_HERITAGE_SYSTEM,
                                   PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete the respondent notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION,
                                   NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_ACTIVITY_ID
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }
}
