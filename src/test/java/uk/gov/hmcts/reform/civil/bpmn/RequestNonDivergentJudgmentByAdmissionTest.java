package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RequestNonDivergentJudgmentByAdmissionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_ID";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID = "JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER";
    public static final String JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID = "PostPINInLetterLIPDefendant";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT";
    public static final String GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT = "GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT";
    public static final String SEND_JUDGMENT_DETAILS_EVENT = "SEND_JUDGMENT_DETAILS_CJES";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocClaimant";
    public static final String GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID = "GenerateJudgmentByAdmissionDocDefendant";
    public static final String SEND_JUDGMENT_DETAILS_ACTIVITY_ID = "SendJudgmentDetailsToCJES";

    public RequestNonDivergentJudgmentByAdmissionTest() {
        super("judgment_by_admission_non_divergent_spec.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteRequestJudgmentByAdmission(boolean isLiPDefendant) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", false,
            "DASHBOARD_SERVICE_ENABLED", true,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables);

        ExternalTask claimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_JUDGMENT_BY_ADMISSION",
            "NotifyClaimantJudgmentByAdmission"
        );

        ExternalTask defendantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_JUDGMENT_BY_ADMISSION",
            "NotifyDefendantJudgmentByAdmission"
        );

        ExternalTask generateDocClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDocClaimant,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_CLAIMANT_ACTIVITY_ID
        );

        ExternalTask generateDocDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateDocDefendant,
            PROCESS_CASE_EVENT,
            GEN_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_EVENT,
            GENERATE_JUDGMENT_BY_ADMISSION_DOC_DEFENDANT_ACTIVITY_ID
        );

        ExternalTask sendJudgmentDetailsToCJES = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgmentDetailsToCJES,
            PROCESS_CASE_EVENT,
            SEND_JUDGMENT_DETAILS_EVENT,
            SEND_JUDGMENT_DETAILS_ACTIVITY_ID
        );

        if (isLiPDefendant) {
            ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent1Notification,
                PROCESS_CASE_EVENT,
                JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_EVENT_ID,
                JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER_ACTIVITY_ID,
                variables
            );

            //complete the notification dashboard
            ExternalTask dashboardDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardDefendant,
                PROCESS_CASE_EVENT,
                "CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT",
                "GenerateDashboardNotificationJudgementByAdmissionDefendant",
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
