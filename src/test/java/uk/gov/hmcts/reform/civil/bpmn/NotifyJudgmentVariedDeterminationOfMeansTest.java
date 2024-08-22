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

class NotifyJudgmentVariedDeterminationOfMeansTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS";
    public static final String PROCESS_ID = "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS";

    public NotifyJudgmentVariedDeterminationOfMeansTest() {
        super("notify_judgment_varied_determination_of_means.bpmn", "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS");
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,false", "true,true", "false,true"})
    void shouldSuccessfullyNotifyJudgmentVariedDeterminationOfMeans(boolean twoRepresentatives, boolean isLiPDefendant) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant));
        variables.put("judgmentRecordedReason", true);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //complete call to CJES for default Judgment
        ExternalTask sendJudgmentDetailsToCJES = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentDetailsToCJES,
            PROCESS_CASE_EVENT,
            "SEND_JUDGMENT_DETAILS_CJES",
            "SendJudgmentDetailsToCJES"
        );

        //complete the notification to Claimant
        ExternalTask claimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS",
            "NotifyClaimantJudgmentVariedDeterminationOfMeans"
        );

        if (!isLiPDefendant) {
            //complete the notification to Respondent
            ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent1Notification,
                PROCESS_CASE_EVENT,
                "NOTIFY_SOLICITOR1_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS",
                "NotifyDefendantVariedDeterminationOfMeans1",
                variables
            );

            if (twoRepresentatives) {
                //complete the notification to Respondent2
                ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    respondent2Notification,
                    PROCESS_CASE_EVENT,
                    "NOTIFY_SOLICITOR2_DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS",
                    "NotifyDefendantVariedDeterminationOfMeans2",
                    variables
                );
            }
        }

        if (isLiPDefendant) {
            //complete the notification to Respondent
            ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent1Notification,
                PROCESS_CASE_EVENT,
                "NOTIFY_DEFENDANT1_LIP_JUDGMENT_VARIED_DETERMINATION_OF_MEANS",
                "NotifyDefendantLipVariedDeterminationOfMeans",
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
