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

    private static final String MESSAGE_NAME = "DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC";
    private static final String PROCESS_ID = "GENERATE_DJ_NON_DIVERGENT_FORM_SPEC";

    //CCD CASE EVENT
    private static final String GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT = "GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT";
    private static final String GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT = "GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT";
    private static final String SEND_JUDGMENT_DETAILS_TO_CJES = "SEND_JUDGMENT_DETAILS_CJES";
    private static final String NOTIFY_RPA_DJ_SPEC = "NOTIFY_RPA_DJ_SPEC";
    private static final String NOTIFY_EVENT = "NOTIFY_EVENT";

    //ACTIVITY IDs
    private static final String GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecClaimant";
    private static final String GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecDefendant";
    private static final String SEND_JUDGMENT_DETAILS_TO_CJES_ACTIVITY_ID = "SendJudgmentDetailsToCJES";
    private static final String NOTIFY_RPA_FEED_ACTIVITY_ID = "NotifyRPADJSPECID";

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
                "POST_DJ_NON_DIVERGENT_COVER_LETTER_CLAIMANT",
                "PostClaimantDJCoverLetterAndDocument",
                variables
            );
        }

        // Send letter to LiP defendant if needed
        if (isLiPDefendant) {
            ExternalTask sendLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendLipLetter,
                PROCESS_CASE_EVENT,
                "POST_DJ_NON_DIVERGENT_PIN_IN_LETTER_DEFENDANT1",
                "PostPINInLetterLIPDefendant1",
                variables
            );
        }

        // Complete the consolidated notification to all parties
        ExternalTask allPartiesNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            allPartiesNotification,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            "DJ_NON_DIVERGENT_NOTIFIER",
            variables
        );

        if (dashboardServiceEnabled && (isLiPDefendant || isLiPClaimant)) {
            //complete generate dashboard notifications
            ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardNotification,
                PROCESS_CASE_EVENT,
                "DASHBOARD_NOTIFICATION_EVENT",
                "GenerateDashboardNotificationsDJNonDivergent",
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
