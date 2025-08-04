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

class SetAsideJudgmentTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "SET_ASIDE_JUDGMENT";
    public static final String PROCESS_ID = "SET_ASIDE_JUDGMENT";
    public static final String SEND_JUDGMENT_DETAILS_SA_EVENT = "SEND_JUDGMENT_DETAILS_CJES_SA";
    public static final String SEND_JUDGMENT_DETAILS_ACTIVITY_ID = "SendJudgmentDetailsToCJES";

    public static final String JUDGMENT_SET_ASIDE_ERROR = "JUDGMENT_SET_ASIDE_ERROR";

    //CCD CASE EVENT
    public static final String CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGEMENT_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGEMENT_CLAIMANT";

    //ACTIVITY IDs
    public static final String CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGEMENT_CLAIMANT_ACTIVITY_ID = "GenerateDashboardNotificationSetAsideJudgmentClaimant";
    public static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    public static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ID = "NotifyRoboticsOnContinuousFeed";

    public SetAsideJudgmentTest() {
        super("set_aside_judgment_request.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true, true, true, true",
        "true, true, true, false, true, true",
        "true, true, false, true, false, true",
        "true, true, false, false, false, false",
        "true, false, true, true, true, true",
        "true, false, true, false, false, false",
        "true, false, false, true, true, true",
        "true, false, false, false, false, false",
        "false, true, true, true, true, true",
        "false, true, true, false, false, false",
        "false, true, false, true, true, true",
        "false, true, false, false, false, false",
        "false, false, true, true, true, true",
        "false, false, true, false, false, false",
        "false, false, false, true, true, true",
        "false, false, false, false, false, false"
    })
    void shouldSuccessfullyNotifySetAsideJudgmentRequest(boolean twoRepresentatives,
                                                         boolean isLiPDefendant,
                                                         boolean dashboardServiceEnabled,
                                                         boolean judgmentSetAsideError,
                                                         boolean isJoFeedLive,
                                                         boolean isCjesServiceEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled,
            IS_JO_LIVE_FEED_ACTIVE, isJoFeedLive,
            IS_CJES_SERVICE_ENABLED, isCjesServiceEnabled));
        variables.put(JUDGMENT_SET_ASIDE_ERROR, judgmentSetAsideError);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (isCjesServiceEnabled) {
            //complete the call to CJES for Set Aside Judgment
            ExternalTask sendJudgmentDetailsToCJES = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendJudgmentDetailsToCJES,
                PROCESS_CASE_EVENT,
                SEND_JUDGMENT_DETAILS_SA_EVENT,
                SEND_JUDGMENT_DETAILS_ACTIVITY_ID
            );
        }

        if (judgmentSetAsideError) {
            //complete the notification to Claimant
            ExternalTask claimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimantNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_CLAIMANT",
                "NotifyClaimSetAsideJudgmentClaimant"
            );

            //complete generate dashboard notification to claimant
            ExternalTask claimant1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimant1DashboardNotification,
                PROCESS_CASE_EVENT,
                CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGEMENT_CLAIMANT,
                CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGEMENT_CLAIMANT_ACTIVITY_ID,
                variables
            );

            if (!isLiPDefendant) {
                //complete the notification to Respondent
                ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    respondent1Notification,
                    PROCESS_CASE_EVENT,
                    "NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1",
                    "NotifyClaimSetAsideJudgmentDefendant1",
                    variables
                );

                if (twoRepresentatives) {
                    //complete the notification to Respondent2
                    ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
                    assertCompleteExternalTask(
                        respondent2Notification,
                        PROCESS_CASE_EVENT,
                        "NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2",
                        "NotifyClaimSetAsideJudgmentDefendant2",
                        variables
                    );
                }
            } else if (isLiPDefendant) {
                //complete the notification to LiP respondent
                ExternalTask respondent1LIpNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    respondent1LIpNotification,
                    PROCESS_CASE_EVENT,
                    "NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP",
                    "NotifyClaimSetAsideJudgmentDefendant1LiP",
                    variables
                );

                // should send letter to LiP respondent
                ExternalTask sendLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    sendLipLetter,
                    PROCESS_CASE_EVENT,
                    "SEND_SET_ASIDE_JUDGEMENT_IN_ERROR_LETTER_TO_LIP_DEFENDANT1",
                    "SendSetAsideLiPLetterDef1",
                    variables
                );
                if (dashboardServiceEnabled) {
                    //complete generate dashboard notification to defendant
                    ExternalTask respondent1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                    assertCompleteExternalTask(
                        respondent1DashboardNotification,
                        PROCESS_CASE_EVENT,
                        "CREATE_DASHBOARD_NOTIFICATION_SET_ASIDE_JUDGMENT_DEFENDANT",
                        "GenerateDashboardNotificationSetAsideDefendant",
                        variables
                    );
                }
            }
        }

        if (isJoFeedLive) {
            //Notify RPA
            ExternalTask notifyRPA = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyRPA,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_ON_CONTINUOUS_FEED,
                NOTIFY_RPA_ON_CONTINUOUS_FEED_ID,
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
