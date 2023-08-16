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
    public static final String NOTIFY_CLAIMANT_HEARING_HMC
        = "NOTIFY_CLAIMANT_HEARING_HMC";
    public static final String NOTIFY_DEFENDANT1_HEARING_HMC
        = "NOTIFY_DEFENDANT1_HEARING_HMC";
    public static final String NOTIFY_DEFENDANT2_HEARING_HMC
        = "NOTIFY_DEFENDANT2_HEARING_HMC";
    public static final String CREATE_SERVICE_REQUEST_API_HMC
        = "CREATE_SERVICE_REQUEST_API_HMC";
    public static final String UPDATE_PARTIES_NOTIFIED_HMC
        = "UPDATE_PARTIES_NOTIFIED_HMC";
    public static final String UPDATE_CASE_PROGRESS_HMC
        = "UPDATE_CASE_PROGRESS_HMC";

    //ACTIVITY IDs

    public static final String START_BUSINESS_ACTIVITY
        = "StartHearingNoticeBusinessProcessTaskId";
    public static final String GENERATE_HEARING_NOTICE_HMC_ACTIVITY_ID
        = "GenerateHearingNotice";
    public static final String NOTIFY_CLAIMANT_HEARING_HMC_ACTIVITY_ID
        = "NotifyClaimantSolicitorHearing";
    public static final String NOTIFY_DEFENDANT1_HEARING_HMC_ACTIVITY_ID
        = "NotifyDefendantSolicitor1Hearing";
    public static final String NOTIFY_DEFENDANT2_HEARING_HMC_ACTIVITY_ID
        = "NotifyDefendantSolicitor2Hearing";
    public static final String CREATE_SERVICE_REQUEST_API_HMC_ACTIVITY_ID
        = "ServiceRequestAPI";
    public static final String UPDATE_PARTIES_NOTIFIED_HMC_ACTIVITY_ID
        = "UpdateHMCPartiesNotified";
    public static final String UPDATE_CASE_PROGRESS_HMC_ACTIVITY_ID
        = "UpdateCaseProgress";

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
    @CsvSource({"CASE_PROGRESSION, true, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES",
        "CASE_PROGRESSION, false, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES",
        "JUDICIAL_REFERRAL, true, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES",
        "JUDICIAL_REFERRAL, false, ONE_RESPONDENT_REPRESENTATIVE, TWO_RESPONDENT_REPRESENTATIVES",
        "CASE_PROGRESSION, true, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO",
        "CASE_PROGRESSION, false, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO",
        "JUDICIAL_REFERRAL, true, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO",
        "JUDICIAL_REFERRAL, false, UNREPRESENTED_DEFENDANT_ONE, UNREPRESENTED_DEFENDANT_TWO"})
    void shouldSuccessfullyCompleteGenerateHearingNotice(String caseState, boolean twoRespondents,
                                                         String respondentOne, String respondentTwo) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            respondentOne, !twoRespondents,
            respondentTwo, twoRespondents));

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

        //complete notify claimant solicitor hearing
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING_HMC,
                                   NOTIFY_CLAIMANT_HEARING_HMC_ACTIVITY_ID,
                                   variables
        );

        //complete notify defendant solicitor 1 hearing
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT1_HEARING_HMC,
                                   NOTIFY_DEFENDANT1_HEARING_HMC_ACTIVITY_ID,
                                   variables
        );

        if (twoRespondents) {
            //complete notify defendant solicitor 2 hearing
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask,
                                       PROCESS_CASE_EVENT,
                                       NOTIFY_DEFENDANT2_HEARING_HMC,
                                       NOTIFY_DEFENDANT2_HEARING_HMC_ACTIVITY_ID,
                                       variables
            );
        }

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
