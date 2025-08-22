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

class GenerateNonDivergentSpecDJFormTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "GENERATE_DJ_NON_DIVERGENT_FORM_SPEC";

    //CCD CASE EVENT
    public static final String GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT = "GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT";
    public static final String GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT = "GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT";
    public static final String SEND_JUDGMENT_DETAILS_TO_CJES = "SEND_JUDGMENT_DETAILS_CJES";
    public static final String CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_DEFENDANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT";
    public static final String NOTIFY_RPA_DJ_SPEC = "NOTIFY_RPA_DJ_SPEC";

    //ACTIVITY IDs
    public static final String GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecClaimant";
    public static final String GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecDefendant";
    public static final String SEND_JUDGMENT_DETAILS_TO_CJES_ACTIVITY_ID = "SendJudgmentDetailsToCJES";
    public static final String CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_DEFENDANT_ACTIVITY_ID = "GenerateDashboardNotificationDJNonDivergentDefendant";
    public static final String CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT_ACTIVITY_ID = "GenerateDashboardNotificationDJNonDivergentClaimant";
    public static final String NOTIFY_RPA_FEED_ACTIVITY_ID = "NotifyRPADJSPECID";

    public GenerateNonDivergentSpecDJFormTest() {
        super("generate_non_divergent_spec_DJ_form.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true, true, true, true",
        "true, true, true, false, false, false",
        "true, false, true, true, false, false",
        "true, false, true, false, true, false",
        "false, true, true, true, true, true",
        "false, true, true, false, true, false",
        "false, false, true, true, true, false",
        "false, false, true, false, true, true",
        "true, true, false, true, true, true",
        "true, true, false, false, false, false",
        "true, false, false, true, false, false",
        "true, false, false, false, true, true",
        "false, true, false, true, true, false",
        "false, true, false, false, true, true",
        "false, false, false, true, true, false",
        "false, false, false, false, true, true"
    })
    void shouldSuccessfullyComplete(boolean twoRepresentatives, boolean isLiPDefendant, boolean isLiPClaimant, boolean dashboardServiceEnabled,
                                    boolean isJoFeedLive, boolean isCjesServiceEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            UNREPRESENTED_DEFENDANT_TWO, false,
            DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled,
            IS_JO_LIVE_FEED_ACTIVE, isJoFeedLive,
            IS_CJES_SERVICE_ENABLED, isCjesServiceEnabled,
            LIP_CASE, isLiPClaimant
            ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask docmosisTask;

        docmosisTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            docmosisTask, PROCESS_CASE_EVENT,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT,
            GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID
        );
        docmosisTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            docmosisTask, PROCESS_CASE_EVENT,
            GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT,
            GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID
        );

        if (isCjesServiceEnabled) {
            //complete call to CJES for default Judgment
            ExternalTask sendJudgmentDetailsToCJES = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendJudgmentDetailsToCJES,
                PROCESS_CASE_EVENT,
                SEND_JUDGMENT_DETAILS_TO_CJES,
                SEND_JUDGMENT_DETAILS_TO_CJES_ACTIVITY_ID
            );
        }

        if (isLiPClaimant) {
            // should send letter to LiP claimant
            ExternalTask claimantLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimantLipLetter,
                PROCESS_CASE_EVENT,
                "POST_DJ_NON_DIVERGENT_PIN_IN_LETTER_CLAIMANT",
                "NotifyDJNonDivergentClaimantLiP",
                variables
            );
        }

        // Continue with the rest of the process as before
        if (!isLiPDefendant) {
            //complete the notification to Respondent
            ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent1Notification,
                PROCESS_CASE_EVENT,
                "NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR",
                "NotifyDJNonDivergentDefendant1",
                variables
            );
        } else { // Lip v Lip: different notification
            ExternalTask respondent1LIpNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent1LIpNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP",
                "NotifyDJNonDivergentDefendant1LiP",
                variables
            );

            // should send letter to LiP respondent
            ExternalTask sendLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendLipLetter,
                PROCESS_CASE_EVENT,
                "POST_DJ_NON_DIVERGENT_PIN_IN_LETTER_DEFENDANT1",
                "PostPINInLetterLIPDefendant1",
                variables
            );

            if (dashboardServiceEnabled) {
                //complete generate dashboard notification to defendant
                ExternalTask respondent1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    respondent1DashboardNotification,
                    PROCESS_CASE_EVENT,
                    CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_DEFENDANT,
                    CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_DEFENDANT_ACTIVITY_ID,
                    variables
                );
            }
        }

        if (dashboardServiceEnabled && isLiPClaimant) {
            //complete generate dashboard notification to claimant
            ExternalTask claimantDashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimantDashboardNotification,
                PROCESS_CASE_EVENT,
                CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT,
                CREATE_DASHBOARD_NOTIFICATION_DJ_NON_DIVERGENT_CLAIMANT_ACTIVITY_ID,
                variables
            );
        }

        //complete the notification to Claimant
        ExternalTask claimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_DJ_NON_DIVERGENT_SPEC_CLAIMANT",
            "NotifyDJNonDivergentClaimant"
        );

        if (twoRepresentatives) {
            //complete the notification to Respondent2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent2Notification,
                PROCESS_CASE_EVENT,
                "NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR",
                "NotifyDJNonDivergentDefendant2",
                variables
            );
        }

        if (isJoFeedLive) {
            //Notify RPA
            ExternalTask notifyRPA = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyRPA,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA_DJ_SPEC,
                NOTIFY_RPA_FEED_ACTIVITY_ID,
                variables
            );
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
