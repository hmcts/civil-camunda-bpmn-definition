package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RequestJudgementByAdmissionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "REQUEST_JUDGEMENT_ADMISSION_SPEC";
    public static final String PROCESS_ID = "REQUEST_JUDGEMENT_ADMISSION_SPEC_ID";

    //CCD CASE EVENTS
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM
        = "PROCEEDS_IN_HERITAGE_SYSTEM";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE
        = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    public static final String GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC
        = "GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC";

    //ACTIVITY IDs
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        = "proceedsInHeritageSystem";
    public static final String NOTIFY_PARTIES_ACTIVITY_ID
        = "RequestJudgementByAdmissionNotifyParties";
    public static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    public static final String GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC_ACTIVITY_ID
        = "GenerateJudgmentByAdmissonDoc";
    private static final String DASHBOARD_NOTIFICATION_ACTIVITY_ID = "GenerateDashboardNotificationsRequestJudgementAdmissionSpec";

    public RequestJudgementByAdmissionTest() {
        super("request_judgement_by_admission.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteRequestJudgementByAdmission_withLr() {

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", false,
            "DASHBOARD_SERVICE_ENABLED", true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the proceed offline
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   PROCEEDS_IN_HERITAGE_SYSTEM,
                                   PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete the notify parties
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_ACTIVITY_ID
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );
        ExternalTask dashboardNotificationApplicant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationApplicant,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            DASHBOARD_NOTIFICATION_ACTIVITY_ID
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteRequestJudgementByAdmission_withLipClaimant() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", true,
            "DASHBOARD_SERVICE_ENABLED", true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the proceed offline
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   PROCEEDS_IN_HERITAGE_SYSTEM,
                                   PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete the notify parties
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_ACTIVITY_ID
        );

        //generate the Judgment By Admission Document
        ExternalTask generateDocTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(generateDocTask,
                                   PROCESS_CASE_EVENT,
                                   GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC,
                                   GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC_ACTIVITY_ID
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotification,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            DASHBOARD_NOTIFICATION_ACTIVITY_ID
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
        assertNoExternalTasksLeft();
    }
}