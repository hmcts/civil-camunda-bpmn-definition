package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NotifyJudgmentVariedDeterminationOfMeansTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS";
    public static final String PROCESS_ID = "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";
    public static final String JUDGMENT_VARIED_DETERMINATION_OF_MEANS_NOTIFY_PARTIES = "JudgmentVariedDeterminationOfMeansNotifyParties";
    public static final String GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT = "GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT";
    public static final String GENERATE_DEFENDANT_JUDGMENT_BY_DETERMINATION_DOC = "GenerateDefendantJudgmentByDeterminationDoc";
    public static final String GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT = "GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT";
    public static final String GENERATE_CLAIMANT_JUDGMENT_BY_DETERMINATION_DOC = "GenerateClaimantJudgmentByDeterminationDoc";
    public static final String SEND_JUDGMENT_DETAILS_CJES = "SEND_JUDGMENT_DETAILS_CJES";
    public static final String SEND_JUDGMENT_DETAILS_TO_CJES = "SendJudgmentDetailsToCJES";

    public NotifyJudgmentVariedDeterminationOfMeansTest() {
        super("notify_judgment_varied_determination_of_means.bpmn", "NOTIFY_JUDGMENT_VARIED_DETERMINATION_OF_MEANS");
    }

    @ParameterizedTest
    @CsvSource({"true,false,true", "false,false,false", "true,true,true", "false,true,true"})
    void shouldSuccessfullyNotifyJudgmentVariedDeterminationOfMeans(boolean twoRepresentatives, boolean isLiPDefendant, boolean isCjesServiceEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
                TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
                UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
                IS_CJES_SERVICE_ENABLED, isCjesServiceEnabled));
        variables.put("judgmentRecordedReason", "DETERMINATION_OF_MEANS");

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
            //complete call to CJES for edit Judgment
            ExternalTask sendJudgmentDetailsToCJES = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendJudgmentDetailsToCJES,
                PROCESS_CASE_EVENT,
                SEND_JUDGMENT_DETAILS_CJES,
                SEND_JUDGMENT_DETAILS_TO_CJES
            );
        }

        //generate judgment determination doc for claimant
        ExternalTask claimantDoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                claimantDoc,
                PROCESS_CASE_EVENT,
                GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT,
                GENERATE_CLAIMANT_JUDGMENT_BY_DETERMINATION_DOC
        );
        //generate judgment determination doc for defendant
        ExternalTask defendantDoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                defendantDoc,
                PROCESS_CASE_EVENT,
                GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT,
                GENERATE_DEFENDANT_JUDGMENT_BY_DETERMINATION_DOC
        );
        //complete the notification to Claimant
        ExternalTask claimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                claimantNotification,
                PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                JUDGMENT_VARIED_DETERMINATION_OF_MEANS_NOTIFY_PARTIES,
                variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldBypassProcessesWhenJudgementRecordedReasonIsNotDeterminationOfMeans(boolean isCjesServiceEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(IS_CJES_SERVICE_ENABLED, isCjesServiceEnabled));
        variables.put("judgmentRecordedReason", "SOMETHING_ELSE"
        );

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
            ExternalTask sendJudgement = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendJudgement,
                PROCESS_CASE_EVENT,
                SEND_JUDGMENT_DETAILS_CJES,
                SEND_JUDGMENT_DETAILS_TO_CJES
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
