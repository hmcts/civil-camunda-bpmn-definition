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
    public static final String NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION
        = "NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION";
    public static final String NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT
        = "NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT";
    public static final String NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT
        = "NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT";
    public static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE
        = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    public static final String GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC
        = "GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC";

    //ACTIVITY IDs
    public static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        = "proceedsInHeritageSystem";
    public static final String NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_ACTIVITY_ID
        = "RequestJudgementByAdmissionNotifyRespondent1";
    public static final String NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT_ACTIVITY_ID
        = "RequestJudgementByAdmissionLipClaimantNotifyApplicant1";
    public static final String NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT_ACTIVITY_ID
        = "RequestJudgementByAdmissionLipClaimantNotifyRespondent1";
    public static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    public static final String GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC_ACTIVITY_ID
        = "GenerateJudgmentByAdmissonDoc";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1 = "CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1_ACTIVITY_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForApplicant1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1 = "CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForRespondent1";

    public RequestJudgementByAdmissionTest() {
        super("request_judgement_by_admission.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteRequestJudgementByAdmission_withLr() {

        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", false));

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

    @Test
    void shouldSuccessfullyCompleteRequestJudgementByAdmission_withLipClaimant() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", true));

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

        //complete the applicant lip notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT,
                                   NOTIFY_APPLICANT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT_ACTIVITY_ID
        );

        //complete the respondent lip notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT,
                                   NOTIFY_RESPONDENT1_FOR_REQUEST_JUDGEMENT_BY_ADMISSION_LIP_CLAIMANT_ACTIVITY_ID
        );

        //generate the Judgment By Admission Document
        ExternalTask generateDocTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(generateDocTask,
                                   PROCESS_CASE_EVENT,
                                   GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC,
                                   GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC_ACTIVITY_ID
        );

        ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotification,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1_ACTIVITY_ID
        );

        ExternalTask dashboardNotificationRespondent1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationRespondent1,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID
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
