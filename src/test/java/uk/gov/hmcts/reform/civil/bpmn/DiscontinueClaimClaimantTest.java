package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DiscontinueClaimClaimantTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DISCONTINUE_CLAIM_CLAIMANT";
    public static final String PROCESS_ID = "DISCONTINUE_CLAIM_CLAIMANT";

    //CCD CASE EVENT
    public static final String GEN_NOTICE_OF_DISCONTINUANCE = "GEN_NOTICE_OF_DISCONTINUANCE";
    public static final String NOTIFY_DISCONTINUANCE_DEFENDANT1 = "NOTIFY_DISCONTINUANCE_DEFENDANT1";
    public static final String NOTIFY_DISCONTINUANCE_CLAIMANT1 = "NOTIFY_DISCONTINUANCE_CLAIMANT1";
    public static final String SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1 = "SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1";
    public static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE = "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE";
    public static final String NOTIFY_DISCONTINUANCE_DEFENDANT2 = "NOTIFY_DISCONTINUANCE_DEFENDANT2";

    //ACTIVITY IDs
    public static final String GEN_NOTICE_OF_DISCONTINUANCE_ACTIVITY_ID = "GenerateNoticeOfDiscontinuance";
    public static final String NOTIFY_DISCONTINUANCE_DEFENDANT1_ACTIVITY_ID = "NotifyDiscontinuancetDefendant1";
    public static final String NOTIFY_DISCONTINUANCE_CLAIMANT1_ACTIVITY_ID = "NotifyDiscontinuanceClaimant";
    public static final String SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1_ACTIVITY_ID = "PostNoticeOfDiscontinuanceDefendant1LIP";
    public static final String DEFENDANT_LIP_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE_ACTIVITY_ID = "CreateDefendantDashboardNotificationsForDiscontinuance";
    public static final String NOTIFY_DISCONTINUANCE_DEFENDANT2_ACTIVITY_ID = "NotifyDiscontinuanceDefendant2";

    public DiscontinueClaimClaimantTest() {
        super("discontinue_claim_claimant.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true",
        "true, true, false",
        "true, false, true",
        "true, false, false",
        "false, true, false",
        "false, false, false",
        "false, true, true",
        "false, false, true"
    })
    void shouldSuccessfullyComplete(boolean isJudgeOrderVerificationRequired, boolean isLiPDefendant, boolean twoDefendants) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            TWO_RESPONDENT_REPRESENTATIVES, twoDefendants,
            UNREPRESENTED_DEFENDANT_TWO, !twoDefendants
        ));
        variables.put("JUDGE_ORDER_VERIFICATION_REQUIRED", isJudgeOrderVerificationRequired);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask noticeTask;

        //complete generate notice of discontinuance activity
        noticeTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(noticeTask, PROCESS_CASE_EVENT,
                                   GEN_NOTICE_OF_DISCONTINUANCE,
                                   GEN_NOTICE_OF_DISCONTINUANCE_ACTIVITY_ID,
                                   variables
        );

        if (!isJudgeOrderVerificationRequired) {
            //complete generate dashboard notification to defendant
            ExternalTask notifyDiscontinuanceDefendant1 = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyDiscontinuanceDefendant1,
                PROCESS_CASE_EVENT,
                NOTIFY_DISCONTINUANCE_DEFENDANT1,
                NOTIFY_DISCONTINUANCE_DEFENDANT1_ACTIVITY_ID,
                variables
            );

            if (isLiPDefendant) {
                //complete the notification to respondant 1 LIP
                ExternalTask postNoticeDiscontinuanceTask = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    postNoticeDiscontinuanceTask,
                    PROCESS_CASE_EVENT,
                    SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1,
                    SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1_ACTIVITY_ID,
                    variables
                );

                //complete the dashboard notification to Defendant 1 LIP
                ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    dashboardNotification,
                    PROCESS_CASE_EVENT,
                    CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE,
                    DEFENDANT_LIP_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE_ACTIVITY_ID,
                    variables
                );

            }

            //complete the notification to claimant
            ExternalTask claimant1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimant1Notification,
                PROCESS_CASE_EVENT,
                NOTIFY_DISCONTINUANCE_CLAIMANT1,
                NOTIFY_DISCONTINUANCE_CLAIMANT1_ACTIVITY_ID,
                variables
            );

            if (twoDefendants) {
                //complete the notification to claimant
                ExternalTask defendant2LRNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    defendant2LRNotification,
                    PROCESS_CASE_EVENT,
                    "NOTIFY_DISCONTINUANCE_DEFENDANT2",
                    "NotifyDiscontinuanceDefendant2",
                    variables
                );
            }

        }

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
