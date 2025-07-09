package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GenerateHearingNoticeTest extends BpmnBaseTest {

    private static final String DIAGRAM_PATH = "camunda/%s";
    public static final String MESSAGE_NAME = "NOTIFY_HEARING_PARTIES";
    public static final String PROCESS_ID = "NOTIFY_HEARING_PARTIES";

    //CCD CASE EVENT
    public static final String START_BUSINESS_TOPIC
            = "START_HEARING_NOTICE_BUSINESS_PROCESS";
    public static final String START_BUSINESS_EVENT
            = "START_BUSINESS_PROCESS";
    public static final String GENERATE_HEARING_NOTICE_HMC
            = "GENERATE_HEARING_NOTICE_HMC";
    public static final String NOTIFY_EVENT
            = "NOTIFY_EVENT";
    public static final String CREATE_SERVICE_REQUEST_API_HMC
            = "CREATE_SERVICE_REQUEST_API_HMC";
    public static final String UPDATE_PARTIES_NOTIFIED_HMC
            = "UPDATE_PARTIES_NOTIFIED_HMC";
    public static final String UPDATE_CASE_PROGRESS_HMC
            = "UPDATE_CASE_PROGRESS_HMC";
    public static final String SEND_HEARING_TO_LIP_DEFENDANT
            = "SEND_HEARING_TO_LIP_DEFENDANT_HMC";
    public static final String SEND_HEARING_TO_LIP_CLAIMANT
            = "SEND_HEARING_TO_LIP_CLAIMANT_HMC";
    public static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT
            = "CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_HMC";
    public static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT
            = "CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT";

    //ACTIVITY IDs
    public static final String START_BUSINESS_ACTIVITY
            = "StartHearingNoticeBusinessProcessTaskId";
    public static final String GENERATE_HEARING_NOTICE_HMC_ACTIVITY_ID
            = "GenerateHearingNotice";
    public static final String HEARING_NOTICE_GENERATOR_HMC_NOTIFIER
            = "HearingNoticeGeneratorHMCNotifier";
    public static final String CREATE_SERVICE_REQUEST_API_HMC_ACTIVITY_ID
            = "ServiceRequestAPI";
    public static final String UPDATE_PARTIES_NOTIFIED_HMC_ACTIVITY_ID
            = "UpdateHMCPartiesNotified";
    public static final String UPDATE_CASE_PROGRESS_HMC_ACTIVITY_ID
            = "UpdateCaseProgress";
    private static final String SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID
            = "SendAutomaticHearingToDefendantLIP";
    private static final String SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID
            = "SendAutomaticHearingToClaimantLIP";
    private static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID
            = "GenerateDashboardNotificationHearingScheduledClaimant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID
            = "GenerateDashboardNotificationHearingScheduledDefendant";

    public GenerateHearingNoticeTest() {
        super("generate_hearing_notice.bpmn", PROCESS_ID);
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, "start_hearing_notice_business_process.bpmn"))
                .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, "end_business_process.bpmn"))
                .deploy();
        deployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
                .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @ParameterizedTest
    @CsvSource({"CASE_PROGRESSION, true, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES, false",
            "CASE_PROGRESSION, false, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES, false",
            "JUDICIAL_REFERRAL, true, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES, false",
            "JUDICIAL_REFERRAL, false, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES, false",
            "CASE_PROGRESSION, true, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO, false",
            "CASE_PROGRESSION, false, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO, true",
            "JUDICIAL_REFERRAL, true, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO, false",
            "JUDICIAL_REFERRAL, false, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO, false"})
    void shouldSuccessfullyCompleteGenerateHearingNotice(String caseState, boolean twoRespondents,
                                                         String respondentOne, String respondentTwo,
                                                         boolean lipCase) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                respondentOne, !twoRespondents,
                respondentTwo, twoRespondents,
                LIP_CASE, lipCase,
                DASHBOARD_SERVICE_ENABLED, true,
                CASE_PROGRESSION_ENABLED, true));

        variables.put("caseState", caseState);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete generate hearing notice
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                PROCESS_CASE_EVENT,
                GENERATE_HEARING_NOTICE_HMC,
                GENERATE_HEARING_NOTICE_HMC_ACTIVITY_ID,
                variables
        );

        if (lipCase) {
            //complete the bulk print
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                    SEND_HEARING_TO_LIP_CLAIMANT, SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID,
                    variables
            );
        }

        if (respondentOne.equals("UNREPRESENTED_DEFENDANT_ONE") && !twoRespondents) {
            //complete the bulk print
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                    SEND_HEARING_TO_LIP_DEFENDANT, SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID,
                    variables
            );
        }

        //complete notify relevant parties
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                HEARING_NOTICE_GENERATOR_HMC_NOTIFIER,
                variables
        );

        //complete service request API
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                PROCESS_CASE_EVENT,
                CREATE_SERVICE_REQUEST_API_HMC,
                CREATE_SERVICE_REQUEST_API_HMC_ACTIVITY_ID,
                variables
        );

        //complete update HMC parties notified
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                PROCESS_CASE_EVENT,
                UPDATE_PARTIES_NOTIFIED_HMC,
                UPDATE_PARTIES_NOTIFIED_HMC_ACTIVITY_ID,
                variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT,
                CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the dashboard notification process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT,
                CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_DEFENDANT_ACTIVITY_ID, variables
        );

        if (caseState.equals("CASE_PROGRESSION")) {
            //complete update case progress
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                    UPDATE_CASE_PROGRESS_HMC,
                    UPDATE_CASE_PROGRESS_HMC_ACTIVITY_ID,
                    variables
            );
        }
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
