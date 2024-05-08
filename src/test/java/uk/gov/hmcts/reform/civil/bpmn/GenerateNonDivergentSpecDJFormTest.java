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
    public static final String POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1 = "POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1";
    public static final String POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2 = "POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2";

    //ACTIVITY IDs
    public static final String GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecClaimant";
    public static final String GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID = "GenerateDJFormNondivergentSpecDefendant";
    public static final String POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1_ACTIVITY_ID = "PostDjLetterDefendant1";
    public static final String POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2_ACTIVITY_ID = "PostDjLetterDefendant2";

    public GenerateNonDivergentSpecDJFormTest() {
        super("generate_non_divergent_spec_DJ_form.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,false", "false,true", "true,true"})
    void shouldSuccessfullyComplete(boolean twoRepresentatives, boolean isLiPDefendant) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            UNREPRESENTED_DEFENDANT_TWO, false));

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

        //complete generate dj form claimant spec activity
        docmosisTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(docmosisTask, PROCESS_CASE_EVENT,
                                   GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT,
                                   GENERATE_DJ_CLAIMANT_FORM_SPEC_ACTIVITY_ID
        );
        //complete generate dj form claimant spec activity
        docmosisTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(docmosisTask, PROCESS_CASE_EVENT,
                                   GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT,
                                   GENERATE_DJ_DEFENDANT_FORM_SPEC_ACTIVITY_ID
        );

        //end business process

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

            //complete the "Post DJ letter defendant1" process
            ExternalTask postDjLetter1 = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                postDjLetter1,
                PROCESS_CASE_EVENT,
                POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1,
                POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1_ACTIVITY_ID,
                variables
            );

        } else if (isLiPDefendant) {
            //complete the notification to LiP respondent
            ExternalTask respondent1LIpNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent1LIpNotification,
                PROCESS_CASE_EVENT,
                "NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP",
                "NotifyDJNonDivergentDefendant1LiP",
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

            //complete the "Post DJ letter defendant2" process
            ExternalTask postDjLetter2 = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                postDjLetter2,
                PROCESS_CASE_EVENT,
                POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2,
                POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2_ACTIVITY_ID,
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
