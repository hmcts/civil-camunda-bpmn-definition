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

    public static final String GEN_NOTICE_OF_DISCONTINUANCE = "GEN_NOTICE_OF_DISCONTINUANCE";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1 = "SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1";
    public static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE =
        "DASHBOARD_NOTIFICATION_EVENT";

    public static final String GEN_NOTICE_OF_DISCONTINUANCE_ACTIVITY_ID = "GenerateNoticeOfDiscontinuance";
    public static final String NOTIFY_DISCONTINUANCE_PARTIES_ACTIVITY_ID = "DiscontinuanceClaimNotifyParties";
    public static final String SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1_ACTIVITY_ID =
        "PostNoticeOfDiscontinuanceDefendant1LIP";
    public static final String DEFENDANT_LIP_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE_ACTIVITY_ID =
        "GenerateDashboardNotificationsDiscontinuance";

    public DiscontinueClaimClaimantTest() {
        super("discontinue_claim_claimant.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true, false",  // Judge order required, LiP defendant, two defendants, Welsh disabled
        "true, true, false, false", // Judge order required, LiP defendant, one defendant, Welsh disabled
        "true, false, true, false", // Judge order required, LR defendant, two defendants, Welsh disabled
        "true, false, false, false", // Judge order required, LR defendant, one defendant, Welsh disabled
        "true, false, false, true", // Judge order required, LR defendant, one defendant, Welsh enabled
        "false, true, false, false", // No judge order, LiP defendant, one defendant, Welsh disabled
        "false, false, false, false", // No judge order, LR defendant, one defendant, Welsh disabled
        "false, true, true, false", // No judge order, LiP defendant, two defendants, Welsh disabled
        "false, false, true, false", // No judge order, LR defendant, two defendants, Welsh disabled
        "false, true, false, true"  // No judge order, LiP defendant, one defendant, Welsh enabled
    })
    void shouldSuccessfullyComplete(boolean isJudgeOrderVerificationRequired, boolean isLiPDefendant,
                                    boolean twoDefendants, boolean welshEnabled) {

        // Assert process has started
        assertFalse(processInstance.isEnded());

        // Assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            TWO_RESPONDENT_REPRESENTATIVES, twoDefendants,
            UNREPRESENTED_DEFENDANT_TWO, !twoDefendants
        ));
        variables.put("JUDGE_ORDER_VERIFICATION_REQUIRED", isJudgeOrderVerificationRequired);
        variables.put("WELSH_ENABLED", welshEnabled);

        // Complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        // Complete generate notice of discontinuance activity
        ExternalTask noticeTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(noticeTask, PROCESS_CASE_EVENT,
                                   GEN_NOTICE_OF_DISCONTINUANCE,
                                   GEN_NOTICE_OF_DISCONTINUANCE_ACTIVITY_ID,
                                   variables
        );

        if (welshEnabled) {
            // Assert flow directly to "End Business Process" due to Welsh-enabled condition
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);
        } else if (isJudgeOrderVerificationRequired) {
            // Assert flow directly to "End Business Process" due to judge order verification required
            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);
        } else {
            ExternalTask notifyDiscontinuanceParties = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyDiscontinuanceParties,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                NOTIFY_DISCONTINUANCE_PARTIES_ACTIVITY_ID,
                variables
            );

            if (isLiPDefendant) {
                // Complete the notification to defendant 1 LiP
                ExternalTask postNoticeDiscontinuanceTask = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    postNoticeDiscontinuanceTask,
                    PROCESS_CASE_EVENT,
                    SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1,
                    SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1_ACTIVITY_ID,
                    variables
                );

                // Complete the dashboard notification to respondent 1 LiP
                ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    dashboardNotification,
                    PROCESS_CASE_EVENT,
                    CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE,
                    DEFENDANT_LIP_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE_ACTIVITY_ID,
                    variables
                );
            }

            ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
            completeBusinessProcess(endBusinessProcess);
        }

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        // Assert process has started
        assertFalse(processInstance.isEnded());

        // Assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        // Fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
